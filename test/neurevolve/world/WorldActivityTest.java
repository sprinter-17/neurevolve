package neurevolve.world;

import neurevolve.organism.Organism;
import static neurevolve.world.Space.EAST;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class WorldActivityTest {

    private WorldConfiguration config;
    private Space frame;
    private World world;
    private int position;
    private Organism organism;

    @Before
    public void setup() {
        config = new WorldConfiguration();
        frame = new Space(10, 10);
        world = new World(n -> n, frame, config);
        position = frame.position(5, 7);
        organism = new Organism(world, 100);
        world.addOrganism(organism, position, EAST);
    }

    @Test
    public void testMove() {
        assertFalse(world.hasOrganism(frame.move(position, EAST)));
        WorldActivity.MOVE.perform(world, organism);
        assertTrue(world.hasOrganism(frame.move(position, EAST)));
    }

    @Test
    public void testEat() {
        config.setConsumptionRate(20);
        world.setResource(position, 100);
        WorldActivity.EAT_HERE.perform(world, organism);
        assertThat(organism.getEnergy(), is(120));
        assertThat(world.getResource(position), is(80));
    }

    @Test
    public void testDivide() {
        WorldActivity.DIVIDE.perform(world, organism);
        assertThat(organism.getEnergy(), is(50));
        assertThat(world.getPopulationSize(), is(2));
    }
}
