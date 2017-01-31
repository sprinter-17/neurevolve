package neurevolve.world;

import neurevolve.organism.Organism;
import static neurevolve.world.Direction.EAST;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class WorldActivityTest {

    private World world;
    private int position;
    private Organism organism;

    @Before
    public void setup() {
        world = new World(n -> n, 10, 10);
        position = world.position(5, 7);
        organism = new Organism(world, 100);
        world.addOrganism(position, organism);
    }

    @Test
    public void testMoveEast() {
        assertFalse(world.hasOrganism(world.move(position, EAST)));
        WorldActivity.MOVE_EAST.perform(world, position, organism);
        assertTrue(world.hasOrganism(world.move(position, EAST)));
    }

    @Test
    public void testEat() {
        world.setTemperatureRange(100, 100);
        world.tick();
        WorldActivity.EAT.perform(world, position, organism);
        assertThat(world.getResource(position), is(80));
        assertThat(organism.getEnergy(), is(120));
    }

    @Test
    public void testDivide() {
        WorldActivity.DIVIDE.perform(world, position, organism);
        assertThat(organism.getEnergy(), is(50));
        assertThat(world.getPopulationSize(), is(2));
    }
}
