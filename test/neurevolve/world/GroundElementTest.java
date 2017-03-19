package neurevolve.world;

import java.util.Random;
import static neurevolve.world.GroundElement.ACID;
import static neurevolve.world.GroundElement.ELEVATION;
import static neurevolve.world.GroundElement.RESOURCES;
import static neurevolve.world.GroundElement.WALL;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class GroundElementTest {

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
        data = WALL.set(data, 0);
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
        data = WALL.set(data, 0);
        data = ELEVATION.set(data, 17);
        data = RESOURCES.set(data, 24);
        assertThat(ELEVATION.get(data), is(17));
        assertThat(RESOURCES.get(data), is(24));
    }

    @Test
    public void testWallCancelsResources() {
        data = WALL.set(data, 0);
        data = RESOURCES.set(data, 5);
        assertThat(RESOURCES.get(data), is(5));
        data = WALL.set(data, 1);
        assertThat(RESOURCES.get(data), is(0));
        data = RESOURCES.set(data, 5);
        assertThat(RESOURCES.get(data), is(0));
    }

}
