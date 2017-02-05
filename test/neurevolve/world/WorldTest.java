package neurevolve.world;

import neurevolve.organism.Instruction;
import neurevolve.organism.Organism;
import neurevolve.organism.Recipe;
import static neurevolve.world.Angle.FORWARD;
import static neurevolve.world.Space.EAST;
import static neurevolve.world.Space.NORTH;
import static neurevolve.world.Space.WEST;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class WorldTest {

    private WorldConfiguration config;
    private Space frame;
    private World world;

    @Before
    public void setup() {
        config = new WorldConfiguration();
        config.setTimeBetweenSplits(0);
        frame = new Space(10, 10);
        world = new World(n -> n, frame, config);
    }

    @Test
    public void testSlope() {
        int position = frame.position(3, 5);
        Organism organism = new Organism(world, 100);
        world.addOrganism(organism, position, EAST);
        assertThat(world.getSlope(organism, FORWARD), is(0));
        world.setElevation(position, 31);
        assertThat(world.getSlope(organism, FORWARD), is(-31));
        world.setElevation(frame.move(position, EAST), 47);
        assertThat(world.getSlope(organism, FORWARD), is(47 - 31));
    }

    @Test
    public void testResource() {
        assertThat(world.getResource(frame.position(1, 7)), is(0));
    }

    @Test
    public void testHasOrganism() {
        int position = frame.position(2, 7);
        assertFalse(world.hasOrganism(position));
        world.addOrganism(new Organism(world, 100), position, EAST);
        assertTrue(world.hasOrganism(position));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTwoOrganismsToSamePosition() {
        int position = frame.position(1, 4);
        world.addOrganism(new Organism(world, 100), position, EAST);
        world.addOrganism(new Organism(world, 100), position, EAST);
    }

    @Test
    public void testAddOrganism() {
        world.addOrganism(new Organism(world, 5), frame.position(4, 6), EAST);
        assertThat(world.getPopulationSize(), is(1));
        world.addOrganism(new Organism(world, 5), frame.position(3, 6), WEST);
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
        world.addOrganism(organism, frame.position(4, 7), EAST);
        world.tick();
        assertThat(organism.getEnergy(), is(20));
        world.tick();
        assertThat(organism.getEnergy(), is(0));
    }

    @Test
    public void testFeedOrganism() {
        int position = frame.position(4, 7);
        Organism organism = new Organism(world, 50);
        world.addOrganism(organism, position, EAST);
        world.setResource(position, 50);
        config.setConsumptionRate(18);
        world.feedOrganism(organism);
        assertThat(world.getResource(position), is(32));
        assertThat(organism.getEnergy(), is(68));
    }

    @Test
    public void testAttackOrganism() {
        int position = frame.position(4, 7);
        Organism organism = new Organism(world, 100);
        Organism victim = new Organism(world, 30);
        world.addOrganism(organism, position, EAST);
        world.addOrganism(victim, world.getPosition(organism, FORWARD), EAST);
        world.attackOrganism(organism, FORWARD);
        assertThat(organism.getEnergy(), is(115));
        assertThat(world.getResource(world.getPosition(victim)), is(15));
        assertTrue(victim.isDead());
    }

    @Test
    public void testRemoveDeadOrganisms() {
        int position = frame.position(4, 7);
        Organism organism = new Organism(world, 0);
        world.addOrganism(organism, position, EAST);
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
        world.addOrganism(organism, position, NORTH);
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
        world.addOrganism(organism, position, EAST);
        assertThat(world.getPopulationSize(), is(1));
        world.splitOrganism(organism);
        assertThat(world.getPopulationSize(), is(2));
    }

    @Test
    public void testDoNotSplitOrganismIfNoRoom() {
        int position = frame.position(6, 9);
        Organism organism = new Organism(world, 120);
        world.addOrganism(organism, position, EAST);
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
        world.seed(recipe, 100, 5);
        assertThat(world.getPopulationSize(), is(5));
    }

    @Test
    public void testGetElevationAsInput() {
        int position = frame.position(4, 7);
        Organism organism = new Organism(world, 100);
        world.addOrganism(organism, position, EAST);
        assertThat(world.getInput(organism, WorldInput.LOOK_SLOPE_FORWARD.ordinal()), is(0));
        world.setElevation(world.getPosition(organism, FORWARD), 11);
        assertThat(world.getInput(organism, WorldInput.LOOK_SLOPE_FORWARD.ordinal()), is(11));
    }

    @Test
    public void testMoveOrganismAsActivity() {
        int position = frame.position(4, 7);
        Organism organism = new Organism(world, 50);
        world.addOrganism(organism, position, EAST);
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
        world.addOrganism(organism, frame.position(5, 5), EAST);
        world.tick();
        assertFalse(world.hasOrganism(frame.position(5, 5)));
    }

    @Test
    public void testInitialGrowth() {
        Recipe recipe = new Recipe();
        recipe.add(Instruction.ADD_NEURON, 0);
        recipe.add(Instruction.SET_ACTIVITY, WorldActivity.DIVIDE.ordinal());
        world.seed(recipe, 100, 5);
        assertThat(world.getPopulationSize(), is(5));
        world.tick();
        assertThat(world.getPopulationSize(), is(10));
        world.tick();
        assertThat(world.getPopulationSize(), is(20));
    }

}
