package neurevolve.world;

import java.util.EnumSet;
import java.util.stream.IntStream;
import neurevolve.TestConfiguration;
import static neurevolve.organism.Code.fromInt;
import neurevolve.organism.Instruction;
import neurevolve.organism.Organism;
import neurevolve.organism.Recipe;
import static neurevolve.world.Angle.FORWARD;
import neurevolve.world.Configuration.Value;
import static neurevolve.world.Configuration.Value.MAX_ENERGY;
import static neurevolve.world.GroundElement.*;
import static neurevolve.world.Space.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class WorldTest {

    private Configuration config;
    private Space space;
    private World world;

    @Before
    public void setup() {
        config = new TestConfiguration();
        space = new Space(10, 10);
        world = new World(n -> n, space, config);
    }

    @Test
    public void testSlope() {
        int position = space.position(3, 5);
        Organism organism = new Organism(world, 100);
        world.addOrganism(organism, position, EAST);
        assertThat(world.getSlope(organism, world.getPosition(organism, FORWARD)), is(0));
        world.addElementValue(position, ELEVATION, 31);
        assertThat(world.getSlope(organism, world.getPosition(organism, FORWARD)), is(-31));
        world.addElementValue(space.move(position, EAST), ELEVATION, 47);
        assertThat(world.getSlope(organism, world.getPosition(organism, FORWARD)), is(47 - 31));
    }

    @Test
    public void testResource() {
        assertThat(world.getElementValue(space.position(1, 7), RESOURCES), is(0));
    }

    @Test
    public void testAcid() {
        assertThat(world.getElementValue(space.position(4, 5), ACID), is(0));
    }

    @Test
    public void testRadiation() {
        assertThat(world.getElementValue(space.position(4, 7), RADIATION), is(0));
        world.addElementValue(space.position(4, 7), RADIATION, 2);
        assertThat(world.getElementValue(space.position(4, 7), RADIATION), is(2));
    }

    @Test
    public void testHasOrganism() {
        int position = space.position(2, 7);
        assertFalse(world.hasOrganism(position));
        world.addOrganism(new Organism(world, 100), position, EAST);
        assertTrue(world.hasOrganism(position));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTwoOrganismsToSamePosition() {
        int position = space.position(1, 4);
        world.addOrganism(new Organism(world, 100), position, EAST);
        world.addOrganism(new Organism(world, 100), position, EAST);
    }

    @Test
    public void testAddOrganism() {
        world.addOrganism(new Organism(world, 5), space.position(4, 6), EAST);
        assertThat(world.getPopulationSize(), is(1));
        world.addOrganism(new Organism(world, 5), space.position(3, 6), WEST);
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
        config.setValue(Value.MIN_TEMP, 30);
        config.setValue(Value.MAX_TEMP, 80);
        assertThat(world.getTemperature(space.position(0, 5)), is(80));
        assertThat(world.getTemperature(space.position(7, 5)), is(80));
        assertThat(world.getTemperature(space.position(7, 3)), is(60));
        assertThat(world.getTemperature(space.position(4, 7)), is(60));
        assertThat(world.getTemperature(space.position(0, 8)), is(50));
        assertThat(world.getTemperature(space.position(0, 0)), is(30));
    }

    @Test
    public void testTemperatureEffectByElevation() {
        int position = space.position(7, 4);
        world.addElementValue(position, ELEVATION, 8);
        assertThat(world.getTemperature(position), is(- 8));
    }

    @Test
    public void testTemperatureEffectByTimeOfYear() {
        int position = space.position(7, 4);
        config.setValue(Value.YEAR_LENGTH, 8);
        config.setValue(Value.TEMP_VARIATION, 50);
        assertThat(world.getTemperature(position), is(-50));
        world.tick();
        assertThat(world.getTemperature(position), is(-25));
        world.tick();
        assertThat(world.getTemperature(position), is(0));
        world.tick();
        assertThat(world.getTemperature(position), is(+25));
        world.tick();
        assertThat(world.getTemperature(position), is(+50));
        world.tick();
        assertThat(world.getTemperature(position), is(+25));
        world.tick();
        assertThat(world.getTemperature(position), is(0));
        world.tick();
        assertThat(world.getTemperature(position), is(-25));
        world.tick();
        assertThat(world.getTemperature(position), is(-50));
    }

    @Test
    public void testNegativeTemperaturesConsumeEnergyFromOrganisms() {
        config.setValue(Value.MIN_TEMP, -30);
        config.setValue(Value.MAX_TEMP, -30);
        Organism organism = new Organism(world, 50);
        world.addOrganism(organism, space.position(4, 7), EAST);
        world.tick();
        assertThat(organism.getEnergy(), is(20));
        world.tick();
        assertThat(organism.getEnergy(), is(0));
    }

    @Test
    public void testFeedOrganism() {
        int position = space.position(4, 7);
        Organism organism = new Organism(world, 50);
        world.addOrganism(organism, position, EAST);
        world.addElementValue(position, RESOURCES, 50);
        config.setValue(Value.CONSUMPTION_RATE, 18);
        config.setValue(MAX_ENERGY, 70);
        world.feedOrganism(organism);
        assertThat(world.getElementValue(position, RESOURCES), is(32));
        assertThat(organism.getEnergy(), is(68));
        world.feedOrganism(organism);
        assertThat(world.getElementValue(position, RESOURCES), is(30));
        assertThat(organism.getEnergy(), is(70));
    }

    @Test
    public void testAttackOrganism() {
        int position = space.position(4, 7);
        Organism organism = new Organism(world, 100);
        Organism victim = new Organism(world, 30);
        world.addOrganism(organism, position, EAST);
        world.addOrganism(victim, world.getPosition(organism, FORWARD), EAST);
        world.attackOrganism(organism, FORWARD);
        assertThat(organism.getEnergy(), is(130));
        assertTrue(victim.isDead());
    }

    @Test
    public void testRemoveDeadOrganisms() {
        int position = space.position(4, 7);
        Organism organism = new Organism(world, 0);
        world.addOrganism(organism, position, EAST);
        world.tick();
        assertFalse(world.hasOrganism(position));
    }

    @Test
    public void testResourcesGrowBasedOnTemperature() {
        config.setValue(Value.MIN_TEMP, 0);
        config.setValue(Value.MAX_TEMP, 200);
        world.tick();
        assertThat(world.getElementValue(space.position(0, 0), RESOURCES), is(0));
        assertThat(world.getElementValue(space.position(7, 3), RESOURCES), is(1));
        assertThat(world.getElementValue(space.position(4, 5), RESOURCES), is(2));
    }

    @Test
    public void testMoveOrganism() {
        int position = space.position(6, 9);
        Organism organism = new Organism(world, 12);
        world.addOrganism(organism, position, NORTH);
        assertTrue(world.hasOrganism(position));
        assertFalse(world.hasOrganism(space.move(position, NORTH)));
        world.moveOrganism(organism);
        assertFalse(world.hasOrganism(position));
        assertTrue(world.hasOrganism(space.move(position, NORTH)));
    }

    @Test
    public void testSplitOrganims() {
        int position = space.position(6, 9);
        Organism organism = new Organism(world, 1000);
        world.addOrganism(organism, position, EAST);
        assertThat(world.getPopulationSize(), is(1));
        world.splitOrganism(organism);
        assertThat(world.getPopulationSize(), is(2));
    }

    @Test
    public void testDoNotSplitOrganismIfNoRoom() {
        int position = space.position(6, 9);
        Organism organism = new Organism(world, 1000);
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
    public void testSplitInRadiation() {
        int position = space.position(5, 5);
        world.addElementValue(space.move(position, NORTH), RADIATION, 3);
        Organism organism = new Organism(world, 120);
        world.addOrganism(organism, position, NORTH);
        world.moveOrganism(organism);
        assertThat(world.getPopulationSize(), is(2));
    }

    @Test
    public void testGetElevationAsInput() {
        int position = space.position(4, 7);
        Organism organism = new Organism(world, 100);
        world.addOrganism(organism, position, EAST);
        world.setUsedElements(EnumSet.of(GroundElement.ELEVATION));
        assertThat(world.getInput(organism, world.getInputCode("Look Slope Forward").getAsInt()), is(0));
        world.addElementValue(world.getPosition(organism, FORWARD), ELEVATION, 11);
        assertThat(world.getInput(organism, world.getInputCode("Look Slope Forward").getAsInt()), is(11));
    }

    @Test
    public void testMoveOrganismAsActivity() {
        int position = space.position(4, 7);
        Organism organism = new Organism(world, 50);
        world.addOrganism(organism, position, EAST);
        world.tick();
        assertTrue(world.hasOrganism(position));
        world.performActivity(organism, WorldActivity.MOVE.ordinal());
        assertFalse(world.hasOrganism(position));
    }

    @Test
    public void testMoveAsPartOfRecipe() {
        Recipe recipe = new Recipe(0);
        recipe.add(Instruction.ADD_NEURON, fromInt(-10));
        recipe.add(Instruction.SET_ACTIVITY, WorldActivity.MOVE.code());
        Organism organism = new Organism(world, 50, recipe);
        world.addOrganism(organism, space.position(5, 5), EAST);
        world.tick();
        assertFalse(world.hasOrganism(space.position(5, 5)));
    }

    @Test
    public void testActivityCost() {
        Organism organism = new Organism(world, 1000);
        world.addOrganism(organism, space.position(5, 5), NORTH);
        config.setActivityCost(WorldActivity.TURN_LEFT, 10);
        config.setActivityFactor(WorldActivity.TURN_LEFT, 150);
        world.performActivity(organism, WorldActivity.TURN_LEFT.ordinal());
        assertThat(organism.getEnergy(), is(990));
        world.performActivity(organism, WorldActivity.TURN_LEFT.ordinal());
        assertThat(organism.getEnergy(), is(965));
        world.performActivity(organism, WorldActivity.TURN_LEFT.ordinal());
        assertThat(organism.getEnergy(), is(925));
    }

    @Test
    public void testAcidHalfLife() {
        int position = space.position(4, 7);
        world.addElementValue(position, ACID, 1);
        world.tick();
        assertThat(world.getElementValue(position, ACID), is(1));
        config.setHalfLife(ACID, 1);
        world.tick();
        assertThat(world.getElementValue(position, ACID), is(0));
    }

    @Test
    public void testRadiationHalfLife() {
        IntStream.range(0, space.size()).forEach(p -> world.addElementValue(p, RADIATION, 3));
        config.setHalfLife(RADIATION, 10);
        assertThat(totalRadiation(), is(300));
        for (int i = 0; i < 10; i++) {
            world.tick();
        }
        assertTrue(totalRadiation() < 220);
        assertTrue(totalRadiation() > 170);
    }

    private int totalRadiation() {
        return IntStream.range(0, space.size()).map(p -> world.getElementValue(p, RADIATION)).sum();
    }
}
