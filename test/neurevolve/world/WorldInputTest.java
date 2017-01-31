package neurevolve.world;

import neurevolve.organism.Organism;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class WorldInputTest {

    private WorldConfiguration config;
    private World world;
    private int position;
    private Organism organism;

    @Before
    public void setup() {
        config = new WorldConfiguration();
        world = new World(n -> n, new Frame(10, 10), config);
        position = world.position(5, 6);
        organism = new Organism(world, 100);
    }

    @Test
    public void testElevation() {
        assertThat(WorldInput.ELEVATION.getValue(world, position, organism), is(0));
        world.setElevation(position, 17);
        assertThat(WorldInput.ELEVATION.getValue(world, position, organism), is(17));
    }

    @Test
    public void testTemperature() {
        assertThat(WorldInput.TEMPERATURE.getValue(world, position, organism), is(0));
        config.setTemperatureRange(50, 50);
        assertThat(WorldInput.TEMPERATURE.getValue(world, position, organism), is(50));
    }
}
