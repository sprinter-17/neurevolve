package neurevolve.world;

import neurevolve.organism.Instruction;
import neurevolve.organism.Organism;
import neurevolve.organism.Recipe;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class WorldTest {

    private World world;
    private WorldEnvironment environment;

    @Before
    public void setup() {
        world = new World(10, 10);
        environment = new WorldEnvironment(world, n -> n);
    }

    @Test
    public void testSize() {
        assertThat(world.size(), is(100));
        assertThat(new World(16, 2).size(), is(32));
    }

    @Test
    public void testSlope() {
        Position position = new Position(3, 5);
        assertThat(world.getSlope(position, Direction.EAST), is(0));
        world.setElevation(position, 31);
        assertThat(world.getSlope(position, Direction.EAST), is(-31));
        world.setElevation(Direction.EAST.move(position), 47);
        assertThat(world.getSlope(position, Direction.EAST), is(47 - 31));
    }

    @Test
    public void testResource() {
        assertThat(world.getResource(new Position(1, 7)), is(0));
    }

    @Test
    public void testHasOrganism() {
        Position position = new Position(2, 7);
        assertFalse(world.hasOrganism(position));
        world.addOrganism(position, new Organism(environment, 100));
        assertTrue(world.hasOrganism(position));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTwoOrganismsToSamePosition() {
        Position position = new Position(1, 4);
        world.addOrganism(position, new Organism(environment, 100));
        world.addOrganism(position, new Organism(environment, 100));
    }

    @Test
    public void testAddOrganism() {
        world.addOrganism(new Position(4, 6), new Organism(environment, 5));
        assertThat(world.getPopulationSize(), is(1));
        world.addOrganism(new Position(3, 6), new Organism(environment, 5));
        assertThat(world.getPopulationSize(), is(2));
    }

    @Test
    public void testPositionWraps() {
        Position position = new Position(5, 5);
        world.addOrganism(position, new Organism(environment, 100));
        assertTrue(world.hasOrganism(position));
        assertTrue(world.hasOrganism(new Position(15, 5)));
        assertTrue(world.hasOrganism(new Position(5, 15)));
        assertTrue(world.hasOrganism(new Position(-5, 5)));
        assertTrue(world.hasOrganism(new Position(5, -5)));
        assertTrue(world.hasOrganism(new Position(1235, -2325)));
    }

    @Test
    public void testTime() {
        assertThat(world.getTime(), is(0));
        world.tick(environment);
        assertThat(world.getTime(), is(1));
    }

    @Test
    public void testTemperatureEffectByLatitude() {
        world.setTemperatureRange(30, 80);
        assertThat(world.getTemperature(new Position(0, 5)), is(80));
        assertThat(world.getTemperature(new Position(7, 5)), is(80));
        assertThat(world.getTemperature(new Position(7, 3)), is(60));
        assertThat(world.getTemperature(new Position(4, 7)), is(60));
        assertThat(world.getTemperature(new Position(0, 8)), is(50));
        assertThat(world.getTemperature(new Position(0, 0)), is(30));
        assertThat(world.getTemperature(new Position(8, 10)), is(30));
    }

    @Test
    public void testTemperatureEffectByElevation() {
        Position position = new Position(7, 4);
        world.setElevation(position, 8);
        assertThat(world.getTemperature(position), is(- 8));
    }

    @Test
    public void testTemperatureEffectByTimeOfYear() {
        Position position = new Position(7, 4);
        world.setYear(4, 50);
        assertThat(world.getTemperature(position), is(50));
        world.tick(environment);
        assertThat(world.getTemperature(position), is(25));
        world.tick(environment);
        assertThat(world.getTemperature(position), is(0));
        world.tick(environment);
        assertThat(world.getTemperature(position), is(25));
        world.tick(environment);
        assertThat(world.getTemperature(position), is(50));
        world.tick(environment);
        assertThat(world.getTemperature(position), is(25));
        world.tick(environment);
        assertThat(world.getTemperature(position), is(0));
    }

    @Test
    public void testNegativeTemperaturesConsumeEnergyFromOrganisms() {
        world.setTemperatureRange(-30, -30);
        Organism organism = new Organism(environment, 50);
        world.addOrganism(new Position(4, 7), organism);
        world.tick(environment);
        assertThat(organism.getEnergy(), is(20));
        world.tick(environment);
        assertThat(organism.getEnergy(), is(0));
    }

    @Test
    public void testFeedOrganism() {
        Position position = new Position(4, 7);
        Organism organism = new Organism(environment, 50);
        world.setTemperatureRange(50, 50);
        world.addOrganism(position, organism);
        world.tick(environment);
        world.feedOrganism(position, 10);
        assertThat(world.getResource(position), is(40));
        assertThat(organism.getEnergy(), is(60));
    }

    @Test
    public void testKillOrganism() {
        Position position = new Position(4, 7);
        Organism organism = new Organism(environment, 50);
        world.addOrganism(position, organism);
        world.tick(environment);
        world.killOrganism(position);
        assertThat(world.getResource(position), is(50));
        assertTrue(organism.isDead());
        assertFalse(world.hasOrganism(position));
    }

    @Test
    public void testRemoveDeadOrganisms() {
        Position position = new Position(4, 7);
        Organism organism = new Organism(environment, 0);
        world.addOrganism(position, organism);
        world.tick(environment);
        assertFalse(world.hasOrganism(position));
    }

    @Test
    public void testResourcesGrowBasedOnTemperature() {
        world.setTemperatureRange(0, 50);
        world.tick(environment);
        assertThat(world.getResource(new Position(0, 0)), is(0));
        assertThat(world.getResource(new Position(7, 3)), is(30));
        assertThat(world.getResource(new Position(-6, 5)), is(50));
    }

    @Test
    public void testMoveOrganism() {
        Position position = new Position(6, 9);
        world.addOrganism(position, new Organism(environment, 12));
        world.tick(environment);
        assertTrue(world.hasOrganism(position));
        assertFalse(world.hasOrganism(Direction.NORTH.move(position)));
        world.moveOrganism(position, Direction.NORTH);
        world.tick(environment);
        assertFalse(world.hasOrganism(position));
        assertTrue(world.hasOrganism(Direction.NORTH.move(position)));
    }

    @Test
    public void testSplitOrganims() {
        Position position = new Position(6, 9);
        Organism organism = new Organism(environment, 12);
        world.addOrganism(position, organism);
        world.tick(environment);
        assertThat(world.getPopulationSize(), is(1));
        world.splitOrganism(position);
        assertThat(world.getPopulationSize(), is(2));
    }

    @Test
    public void testDoNotSplitOrganismIfNoRoom() {
        Position position = new Position(6, 9);
        Organism organism = new Organism(environment, 120);
        world.addOrganism(position, organism);
        world.tick(environment);
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
        world.seed(environment, recipe, 5);
        assertThat(world.getPopulationSize(), is(5));
    }

    @Test
    public void testInitialGrowth() {
        Recipe recipe = new Recipe();
        recipe.add(Instruction.ADD_NEURON);
        recipe.add(Instruction.SET_THRESHOLD);
        recipe.add(-10);
        recipe.add(Instruction.SET_ACTIVITY);
        recipe.add(WorldActivity.DIVIDE.ordinal());
        world.seed(environment, recipe, 5);
        assertThat(world.getPopulationSize(), is(5));
        world.tick(environment);
        assertThat(world.getPopulationSize(), is(10));
        world.tick(environment);
        assertThat(world.getPopulationSize(), is(20));
    }

}