package neurevolve.world;

import neurevolve.organism.Instruction;
import neurevolve.organism.Organism;
import neurevolve.organism.Recipe;
import static neurevolve.world.Frame.EAST;
import static neurevolve.world.Frame.NORTH;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class WorldTest {

    private WorldConfiguration config;
    private Frame frame;
    private World world;

    @Before
    public void setup() {
        config = new WorldConfiguration();
        frame = new Frame(10, 10);
        world = new World(n -> n, frame, config);
    }

    @Test
    public void testSlope() {
        int position = frame.position(3, 5);
        assertThat(world.getSlope(position, EAST), is(0));
        world.setElevation(position, 31);
        assertThat(world.getSlope(position, EAST), is(-31));
        world.setElevation(frame.move(position, EAST), 47);
        assertThat(world.getSlope(position, EAST), is(47 - 31));
    }

    @Test
    public void testResource() {
        assertThat(world.getResource(frame.position(1, 7)), is(0));
    }

    @Test
    public void testHasOrganism() {
        int position = frame.position(2, 7);
        assertFalse(world.hasOrganism(position));
        world.addOrganism(position, new Organism(world, 100));
        assertTrue(world.hasOrganism(position));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTwoOrganismsToSamePosition() {
        int position = frame.position(1, 4);
        world.addOrganism(position, new Organism(world, 100));
        world.addOrganism(position, new Organism(world, 100));
    }

    @Test
    public void testAddOrganism() {
        world.addOrganism(frame.position(4, 6), new Organism(world, 5));
        assertThat(world.getPopulationSize(), is(1));
        world.addOrganism(frame.position(3, 6), new Organism(world, 5));
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
        assertThat(world.getTemperature(frame.position(0, 5)), is(80));
        assertThat(world.getTemperature(frame.position(7, 5)), is(80));
        assertThat(world.getTemperature(frame.position(7, 3)), is(60));
        assertThat(world.getTemperature(frame.position(4, 7)), is(60));
        assertThat(world.getTemperature(frame.position(0, 8)), is(50));
        assertThat(world.getTemperature(frame.position(0, 0)), is(30));
    }

    @Test
    public void testTemperatureEffectByElevation() {
        int position = frame.position(7, 4);
        world.setElevation(position, 8);
        assertThat(world.getTemperature(position), is(- 8));
    }

    @Test
    public void testTemperatureEffectByTimeOfYear() {
        int position = frame.position(7, 4);
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
        world.addOrganism(frame.position(4, 7), organism);
        world.tick();
        assertThat(organism.getEnergy(), is(20));
        world.tick();
        assertThat(organism.getEnergy(), is(0));
    }

    @Test
    public void testFeedOrganism() {
        int position = frame.position(4, 7);
        Organism organism = new Organism(world, 50);
        world.addOrganism(position, organism);
        world.setResource(position, 50);
        config.setConsumptionRate(18);
        world.feedOrganism(organism);
        assertThat(world.getResource(position), is(32));
        assertThat(organism.getEnergy(), is(68));
    }

    @Test
    public void testKillOrganism() {
        int position = frame.position(4, 7);
        Organism organism = new Organism(world, 50);
        world.addOrganism(position, organism);
        world.killOrganism(position);
        assertThat(world.getResource(position), is(50));
        assertTrue(organism.isDead());
        assertFalse(world.hasOrganism(position));
    }

    @Test
    public void testRemoveDeadOrganisms() {
        int position = frame.position(4, 7);
        Organism organism = new Organism(world, 0);
        world.addOrganism(position, organism);
        world.tick();
        assertFalse(world.hasOrganism(position));
    }

    @Test
    public void testResourcesGrowBasedOnTemperature() {
        config.setTemperatureRange(0, 500);
        world.tick();
        assertThat(world.getResource(frame.position(0, 0)), is(0));
        assertThat(world.getResource(frame.position(7, 3)), is(3));
        assertThat(world.getResource(frame.position(4, 5)), is(5));
    }

    @Test
    public void testMoveOrganism() {
        int position = frame.position(6, 9);
        Organism organism = new Organism(world, 12);
        world.addOrganism(position, organism);
        organism.setDirection(NORTH);
        assertTrue(world.hasOrganism(position));
        assertFalse(world.hasOrganism(frame.move(position, NORTH)));
        world.moveOrganism(organism);
        assertFalse(world.hasOrganism(position));
        assertTrue(world.hasOrganism(frame.move(position, NORTH)));
    }

    @Test
    public void testSplitOrganims() {
        int position = frame.position(6, 9);
        Organism organism = new Organism(world, 12);
        world.addOrganism(position, organism);
        assertThat(world.getPopulationSize(), is(1));
        world.splitOrganism(organism);
        assertThat(world.getPopulationSize(), is(2));
    }

    @Test
    public void testDoNotSplitOrganismIfNoRoom() {
        int position = frame.position(6, 9);
        Organism organism = new Organism(world, 120);
        world.addOrganism(position, organism);
        world.tick();
        assertThat(world.getPopulationSize(), is(1));
        world.splitOrganism(organism);
        world.splitOrganism(organism);
        world.splitOrganism(organism);
        world.splitOrganism(organism);
        assertThat(world.getPopulationSize(), is(5));
        world.splitOrganism(organism);
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
        int position = frame.position(4, 7);
        Organism organism = new Organism(world, 100);
        world.addOrganism(position, organism);
        assertThat(world.getInput(organism, WorldInput.ELEVATION.ordinal()), is(0));
        world.setElevation(position, 11);
        assertThat(world.getInput(organism, WorldInput.ELEVATION.ordinal()), is(11));
    }

    @Test
    public void testMoveOrganismAsActivity() {
        int position = frame.position(4, 7);
        Organism organism = new Organism(world, 50);
        world.addOrganism(position, organism);
        world.tick();
        assertTrue(world.hasOrganism(position));
        world.performActivity(organism, WorldActivity.MOVE.ordinal());
        assertFalse(world.hasOrganism(position));
    }

    @Test
    public void testMoveAsPartOfRecipe() {
        Recipe recipe = new Recipe();
        recipe.add(Instruction.ADD_NEURON, -10);
        recipe.add(Instruction.SET_ACTIVITY, WorldActivity.MOVE.ordinal());
        Organism organism = recipe.make(world, 50);
        world.addOrganism(frame.position(5, 5), organism);
        world.tick();
        assertFalse(world.hasOrganism(frame.position(5, 5)));
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
