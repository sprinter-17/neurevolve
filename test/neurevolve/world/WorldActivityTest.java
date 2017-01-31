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
    private Position position;
    private Organism organism;

    @Before
    public void setup() {
        world = new World(n -> n, 10, 10);
        position = new Position(60, 32);
        organism = new Organism(world, 100);
    }

    @Test
    public void testMoveEast() {
        world.addOrganism(position, new Organism(world, 50));
        world.tick();
        assertFalse(world.hasOrganism(Direction.EAST.move(position)));
        WorldActivity.MOVE_EAST.perform(world, position, organism);
        assertTrue(world.hasOrganism(Direction.EAST.move(position)));
    }

    @Test
    public void testEat() {
        Organism organism = new Organism(world, 50);
        world.setTemperatureRange(100, 100);
        world.addOrganism(position, organism);
        world.tick();
        WorldActivity.EAT.perform(world, position, organism);
        assertThat(world.getResource(position), is(80));
        assertThat(organism.getEnergy(), is(70));
    }

    @Test
    public void testDivide() {
        Organism organism = new Organism(world, 50);
        world.addOrganism(position, organism);
        world.tick();
        WorldActivity.DIVIDE.perform(world, position, organism);
        assertThat(organism.getEnergy(), is(25));
        assertThat(world.getPopulationSize(), is(2));
    }
}
