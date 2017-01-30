package neurevolve.world;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class WorldInputTest {

    private World world;
    private Position position;

    @Before
    public void setup() {
        world = new World(10, 10);
        position = new Position(5, 6);
    }

    @Test
    public void testElevation() {
        assertThat(WorldInput.ELEVATION.getValue(world, position), is(0));
        world.setElevation(position, 17);
        assertThat(WorldInput.ELEVATION.getValue(world, position), is(17));
    }

    @Test
    public void testTemperature() {
        assertThat(WorldInput.TEMPERATURE.getValue(world, position), is(0));
        world.setTemperatureRange(50, 50);
        assertThat(WorldInput.TEMPERATURE.getValue(world, position), is(50));
    }

}
