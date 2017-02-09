package neurevolve.world;

import neurevolve.TestConfiguration;
import neurevolve.organism.Organism;
import static neurevolve.world.Space.EAST;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class WorldInputTest {

    private WorldConfiguration config;
    private Space frame;
    private World world;
    private int position;
    private Organism organism;

    @Before
    public void setup() {
        config = new TestConfiguration();
        frame = new Space(10, 10);
        world = new World(n -> n, frame, config);
        position = frame.position(5, 6);
        organism = new Organism(world, 100);
        world.addOrganism(organism, position, EAST);
    }

    @Test
    public void testElevation() {
        assertThat(WorldInput.LOOK_SLOPE_FORWARD.getValue(world, organism), is(0));
        world.addElevation(frame.move(position, 0), 17);
        assertThat(WorldInput.LOOK_SLOPE_FORWARD.getValue(world, organism), is(17));
    }

    @Test
    public void testTemperature() {
        assertThat(WorldInput.TEMPERATURE.getValue(world, organism), is(0));
        config.setTemperatureRange(50, 50);
        assertThat(WorldInput.TEMPERATURE.getValue(world, organism), is(50));
    }
}
