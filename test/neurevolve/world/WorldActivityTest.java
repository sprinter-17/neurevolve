package neurevolve.world;

import neurevolve.organism.Organism;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class WorldActivityTest {

    private World world;
    private WorldEnvironment environment;
    private Position position;

    @Before
    public void setup() {
        world = new World(10, 10);
        environment = new WorldEnvironment(world, n -> n);
        position = new Position(60, 32);
    }

    @Test
    public void testMoveEast() {
        world.addOrganism(position, new Organism(environment, 50));
        world.tick(environment);
        assertFalse(world.hasOrganism(Direction.EAST.move(position)));
        WorldActivity.MOVE_EAST.perform(world, position);
        assertTrue(world.hasOrganism(Direction.EAST.move(position)));
    }

    @Test
    public void testEat() {
        Organism organism = new Organism(environment, 50);
        world.setTemperatureRange(100, 100);
        world.addOrganism(position, organism);
        world.tick(environment);
        WorldActivity.EAT.perform(world, position);
        assertThat(world.getResource(position), is(80));
        assertThat(organism.getEnergy(), is(70));
    }

    @Test
    public void testDivide() {
        Organism organism = new Organism(environment, 50);
        world.addOrganism(position, organism);
        world.tick(environment);
        WorldActivity.DIVIDE.perform(world, position);
        assertThat(organism.getEnergy(), is(25));
        assertThat(world.getPopulationSize(), is(2));
    }
}
