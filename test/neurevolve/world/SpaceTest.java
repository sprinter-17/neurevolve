package neurevolve.world;

import static neurevolve.world.Space.EAST;
import static neurevolve.world.Space.NORTH;
import static neurevolve.world.Space.SOUTH;
import static neurevolve.world.Space.WEST;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class SpaceTest {

    private Space space;

    @Before
    public void setup() {
        space = new Space(20, 10);
    }

    @Test
    public void testSize() {
        assertThat(space.size(), is(200));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativePosition() {
        space.position(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOutOfRangePosition() {
        space.position(0, 10);
    }

    @Test
    public void testMove() {
        assertThat(space.move(space.position(5, 5), SOUTH), is(space.position(5, 6)));
        assertThat(space.move(space.position(5, 5), WEST), is(space.position(4, 5)));
        assertThat(space.move(space.position(5, 5), EAST), is(space.position(6, 5)));
        assertThat(space.move(space.position(5, 5), NORTH), is(space.position(5, 4)));
    }

    @Test
    public void testPositionWraps() {
        assertThat(space.move(space.position(19, 9), SOUTH), is(space.position(19, 0)));
        assertThat(space.move(space.position(0, 0), WEST), is(space.position(19, 0)));
        assertThat(space.move(space.position(19, 9), EAST), is(space.position(0, 9)));
        assertThat(space.move(space.position(0, 0), NORTH), is(space.position(0, 9)));
    }

}
