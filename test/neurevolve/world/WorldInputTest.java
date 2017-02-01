package neurevolve.world;

import neurevolve.organism.Organism;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class WorldInputTest {

    private WorldConfiguration config;
    private Frame frame;
    private World world;
    private int position;
    private Organism organism;

    @Before
    public void setup() {
        config = new WorldConfiguration();
        frame = new Frame(10, 10);
        world = new World(n -> n, frame, config);
        position = frame.position(5, 6);
        organism = new Organism(world, 100);
        world.addOrganism(position, organism);
    }

    @Test
    public void testElevation() {
        assertThat(WorldInput.ELEVATION.getValue(world, organism), is(0));
        world.setElevation(position, 17);
        assertThat(WorldInput.ELEVATION.getValue(world, organism), is(17));
    }

    @Test
    public void testTemperature() {
        assertThat(WorldInput.TEMPERATURE.getValue(world, organism), is(0));
        config.setTemperatureRange(50, 50);
        assertThat(WorldInput.TEMPERATURE.getValue(world, organism), is(50));
    }
}
