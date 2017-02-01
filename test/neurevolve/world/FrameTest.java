package neurevolve.world;

import static neurevolve.world.Frame.EAST;
import static neurevolve.world.Frame.NORTH;
import static neurevolve.world.Frame.SOUTH;
import static neurevolve.world.Frame.WEST;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class FrameTest {

    private Frame frame;

    @Before
    public void setup() {
        frame = new Frame(20, 10);
    }

    @Test
    public void testSize() {
        assertThat(frame.size(), is(200));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativePosition() {
        frame.position(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOutOfRangePosition() {
        frame.position(0, 10);
    }

    @Test
    public void testMove() {
        assertThat(frame.move(frame.position(5, 5), SOUTH), is(frame.position(5, 4)));
        assertThat(frame.move(frame.position(5, 5), WEST), is(frame.position(4, 5)));
        assertThat(frame.move(frame.position(5, 5), EAST), is(frame.position(6, 5)));
        assertThat(frame.move(frame.position(5, 5), NORTH), is(frame.position(5, 6)));
    }

    @Test
    public void testPositionWraps() {
        assertThat(frame.move(frame.position(0, 0), SOUTH), is(frame.position(0, 9)));
        assertThat(frame.move(frame.position(0, 0), WEST), is(frame.position(19, 0)));
        assertThat(frame.move(frame.position(19, 9), EAST), is(frame.position(0, 9)));
        assertThat(frame.move(frame.position(19, 9), NORTH), is(frame.position(19, 0)));
    }

}
