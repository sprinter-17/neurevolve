package neurevolve.world;

import neurevolve.organism.Instruction;
import neurevolve.organism.Organism;
import neurevolve.organism.Recipe;
import neurevolve.world.Frame.Direction;
import static neurevolve.world.Frame.Direction.EAST;
import static neurevolve.world.Frame.Direction.NORTH;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class WorldTest {

    private WorldConfiguration config;
    private World world;

    @Before
    public void setup() {
        config = new WorldConfiguration();
        world = new World(n -> n, new Frame(10, 10), config);
    }

    @Test
    public void testSlope() {
        int position = world.position(3, 5);
        assertThat(world.getSlope(position, Direction.EAST), is(0));
        world.setElevation(position, 31);
        assertThat(world.getSlope(position, Direction.EAST), is(-31));
        world.setElevation(world.move(position, EAST), 47);
        assertThat(world.getSlope(position, Direction.EAST), is(47 - 31));
    }

    @Test
    public void testResource() {
        assertThat(world.getResource(world.position(1, 7)), is(0));
    }

    @Test
    public void testHasOrganism() {
        int position = world.position(2, 7);
        assertFalse(world.hasOrganism(position));
        world.addOrganism(position, new Organism(world, 100));
        assertTrue(world.hasOrganism(position));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTwoOrganismsToSamePosition() {
        int position = world.position(1, 4);
        world.addOrganism(position, new Organism(world, 100));
        world.addOrganism(position, new Organism(world, 100));
    }

    @Test
    public void testAddOrganism() {
        world.addOrganism(world.position(4, 6), new Organism(world, 5));
        assertThat(world.getPopulationSize(), is(1));
        world.addOrganism(world.position(3, 6), new Organism(world, 5));
        assertThat(world.getPopulationSize(), is(2));
    }

    @Test
    public void testTime() {
        assertThat(world.getTime(), is(0));
        world.tick();
        assertThat(world.getTime(), is(1));
    }

    @Test
    public void testTemperatureEffectByLatitude() {
        config.setTemperatureRange(30, 80);
        assertThat(world.getTemperature(world.position(0, 5)), is(80));
        assertThat(world.getTemperature(world.position(7, 5)), is(80));
        assertThat(world.getTemperature(world.position(7, 3)), is(60));
        assertThat(world.getTemperature(world.position(4, 7)), is(60));
        assertThat(world.getTemperature(world.position(0, 8)), is(50));
        assertThat(world.getTemperature(world.position(0, 0)), is(30));
    }

    @Test
    public void testTemperatureEffectByElevation() {
        int position = world.position(7, 4);
        world.setElevation(position, 8);
        assertThat(world.getTemperature(position), is(- 8));
    }

    @Test
    public void testTemperatureEffectByTimeOfYear() {
        int position = world.position(7, 4);
        config.setYear(4, 50);
        assertThat(world.getTemperature(position), is(50));
        world.tick();
        assertThat(world.getTemperature(position), is(25));
        world.tick();
        assertThat(world.getTemperature(position), is(0));
        world.tick();
        assertThat(world.getTemperature(position), is(25));
        world.tick();
        assertThat(world.getTemperature(position), is(50));
        world.tick();
        assertThat(world.getTemperature(position), is(25));
        world.tick();
        assertThat(world.getTemperature(position), is(0));
    }

    @Test
    public void testNegativeTemperaturesConsumeEnergyFromOrganisms() {
        config.setTemperatureRange(-30, -30);
        Organism organism = new Organism(world, 50);
        world.addOrganism(world.position(4, 7), organism);
        world.tick();
        assertThat(organism.getEnergy(), is(20));
        world.tick();
        assertThat(organism.getEnergy(), is(0));
    }

    @Test
    public void testFeedOrganism() {
        int position = world.position(4, 7);
        Organism organism = new Organism(world, 50);
        config.setTemperatureRange(50, 50);
        world.tick();
        world.addOrganism(position, organism);
        config.setConsumptionRate(18);
        world.feedOrganism(position);
        assertThat(world.getResource(position), is(32));
        assertThat(organism.getEnergy(), is(68));
    }

    @Test
    public void testKillOrganism() {
        int position = world.position(4, 7);
        Organism organism = new Organism(world, 50);
        world.addOrganism(position, organism);
        world.tick();
        world.killOrganism(position);
        assertThat(world.getResource(position), is(50));
        assertTrue(organism.isDead());
        assertFalse(world.hasOrganism(position));
    }

    @Test
    public void testRemoveDeadOrganisms() {
        int position = world.position(4, 7);
        Organism organism = new Organism(world, 0);
        world.addOrganism(position, organism);
        world.tick();
        assertFalse(world.hasOrganism(position));
    }

    @Test
    public void testResourcesGrowBasedOnTemperature() {
        config.setTemperatureRange(0, 50);
        world.tick();
        assertThat(world.getResource(world.position(0, 0)), is(0));
        assertThat(world.getResource(world.position(7, 3)), is(30));
        assertThat(world.getResource(world.position(4, 5)), is(50));
    }

    @Test
    public void testMoveOrganism() {
        int position = world.position(6, 9);
        world.addOrganism(position, new Organism(world, 12));
        world.tick();
        assertTrue(world.hasOrganism(position));
        assertFalse(world.hasOrganism(world.move(position, NORTH)));
        world.moveOrganism(position, Direction.NORTH);
        world.tick();
        assertFalse(world.hasOrganism(position));
        assertTrue(world.hasOrganism(world.move(position, NORTH)));
    }

    @Test
    public void testSplitOrganims() {
        int position = world.position(6, 9);
        Organism organism = new Organism(world, 12);
        world.addOrganism(position, organism);
        world.tick();
        assertThat(world.getPopulationSize(), is(1));
        world.splitOrganism(position);
        assertThat(world.getPopulationSize(), is(2));
    }

    @Test
    public void testDoNotSplitOrganismIfNoRoom() {
        int position = world.position(6, 9);
        Organism organism = new Organism(world, 120);
        world.addOrganism(position, organism);
        world.tick();
        assertThat(world.getPopulationSize(), is(1));
        world.splitOrganism(position);
        world.splitOrganism(position);
        world.splitOrganism(position);
        world.splitOrganism(position);
        assertThat(world.getPopulationSize(), is(5));
        world.splitOrganism(position);
        assertThat(world.getPopulationSize(), is(5));
    }

    @Test
    public void testSeedWorld() {
        Recipe recipe = new Recipe();
        world.seed(recipe, 5);
        assertThat(world.getPopulationSize(), is(5));
    }

    @Test
    public void testGetElevationAsInput() {
        int position = world.position(4, 7);
        world.setCurrentPosition(position);
        assertThat(world.getInput(WorldInput.ELEVATION.ordinal()), is(0));
        world.setElevation(position, 11);
        assertThat(world.getInput(WorldInput.ELEVATION.ordinal()), is(11));
    }

    @Test
    public void testMoveOrganismAsActivity() {
        int position = world.position(4, 7);
        world.setCurrentPosition(position);
        world.addOrganism(position, new Organism(world, 50));
        world.tick();
        assertFalse(world.hasOrganism(world.move(position, EAST)));
        world.performActivity(WorldActivity.MOVE_EAST.ordinal());
        assertTrue(world.hasOrganism(world.move(position, EAST)));
    }

    @Test
    public void testMoveAsPartOfRecipe() {
        Recipe recipe = new Recipe();
        recipe.add(Instruction.ADD_NEURON, -10);
        recipe.add(Instruction.SET_ACTIVITY, WorldActivity.MOVE_EAST.ordinal());
        Organism organism = recipe.make(world, 50);
        world.addOrganism(world.position(5, 5), organism);
        world.tick();
        assertTrue(world.hasOrganism(world.position(6, 5)));
    }

    @Test
    public void testInitialGrowth() {
        Recipe recipe = new Recipe();
        recipe.add(Instruction.ADD_NEURON, 0);
        recipe.add(Instruction.SET_ACTIVITY, WorldActivity.DIVIDE.ordinal());
        world.seed(recipe, 5);
        assertThat(world.getPopulationSize(), is(5));
        world.tick();
        assertThat(world.getPopulationSize(), is(10));
        world.tick();
        assertThat(world.getPopulationSize(), is(20));
    }

}
