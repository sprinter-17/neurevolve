package neurevolve.world;

import java.util.Random;
import static neurevolve.world.World.Data.ACID;
import static neurevolve.world.World.Data.ELEVATION;
import static neurevolve.world.World.Data.RESOURCES;
import static neurevolve.world.World.Data.WALL;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class WorldDataTest {

    private final Random random = new Random();
    private int data;

    @Before
    public void setup() {
        data = random.nextInt();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceedLimit() {
        ELEVATION.set(data, 400);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegative() {
        ELEVATION.set(data, -1);
    }

    @Test
    public void testElevation() {
        assertThat(ELEVATION.get(ELEVATION.set(data, 15)), is(15));
    }

    @Test
    public void testResource() {
        assertThat(RESOURCES.get(RESOURCES.set(data, 80)), is(80));
    }

    @Test
    public void testAcidic() {
        data = ACID.set(data, 1);
        assertThat(ACID.get(data), is(1));
        data = ACID.set(data, 0);
        assertThat(ACID.get(data), is(0));
    }

    @Test
    public void testWall() {
        data = WALL.set(data, 1);
        assertThat(WALL.get(data), is(1));
        data = WALL.set(data, 0);
        assertThat(WALL.get(data), is(0));
    }

    @Test
    public void testSeveralValues() {
        int spot = random.nextInt();
        spot = ELEVATION.set(spot, 17);
        spot = RESOURCES.set(spot, 24);
        assertThat(ELEVATION.get(spot), is(17));
        assertThat(RESOURCES.get(spot), is(24));
    }

}
