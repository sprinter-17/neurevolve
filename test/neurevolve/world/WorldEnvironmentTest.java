package neurevolve.world;

import neurevolve.organism.Organism;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class WorldEnvironmentTest {

    private World world;
    private WorldEnvironment environment;
    private Position position;
    private Organism organism;

    @Before
    public void setup() {
        world = new World(10, 10);
        environment = new WorldEnvironment(world, n -> n);
        position = new Position(5, 5);
        organism = new Organism(environment, 100);
        world.addOrganism(position, organism);
        environment.setContext(position, organism);
        world.tick(environment);
    }

    @Test
    public void testMeaninglessInput() {
        assertThat(environment.getInput(-1), is(0));
    }

    @Test
    public void testGetElevation() {
        assertThat(environment.getInput(WorldInput.ELEVATION.ordinal()), is(0));
        world.setElevation(position, 11);
        assertThat(environment.getInput(WorldInput.ELEVATION.ordinal()), is(11));
    }

    @Test
    public void testMoveOrganism() {
        assertFalse(world.hasOrganism(Direction.EAST.move(position)));
        environment.performActivity(WorldActivity.MOVE_EAST.ordinal());
        assertTrue(world.hasOrganism(Direction.EAST.move(position)));
    }

}
