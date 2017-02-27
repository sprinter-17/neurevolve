package neurevolve.world;

import static neurevolve.world.Space.EAST;
import static neurevolve.world.Space.NORTH;
import static neurevolve.world.Space.SOUTH;
import static neurevolve.world.Space.WEST;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class AngleTest {

    @Test
    public void testAngles() {
        assertThat(Angle.FORWARD.add(EAST), is(EAST));
        assertThat(Angle.LEFT.add(EAST), is(NORTH));
        assertThat(Angle.BACKWARD.add(NORTH), is(SOUTH));
        assertThat(Angle.RIGHT.add(SOUTH), is(WEST));
    }
}
