package neurevolve.world;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class PositionTest {

    @Test
    public void testLatitude() {
        assertThat(new Position(6, 17).latitude(5), is(0));
        assertThat(new Position(127, 17).latitude(5), is(0));
        assertThat(new Position(0, 20).latitude(5), is(-2));
        assertThat(new Position(0, 19).latitude(5), is(2));
    }

    @Test
    public void testMoves() {
        assertThat(new Position(6, 7).east(), is(new Position(7, 7)));
        assertThat(new Position(6, 7).north(), is(new Position(6, 8)));
        assertThat(new Position(6, 7).west(), is(new Position(5, 7)));
        assertThat(new Position(6, 7).south(), is(new Position(6, 6)));
    }

    @Test
    public void testToIndex() {
        testConversions(new Position(0, 0), 0);
        testConversions(new Position(1, 0), 1);
        testConversions(new Position(2, 0), 2);
        testConversions(new Position(3, 0), 3);
        testConversions(new Position(4, 0), 4);
        testConversions(new Position(5, 0), 0);
        testConversions(new Position(0, 1), 5);
        testConversions(new Position(5, 3), 0);
        testConversions(new Position(4, 2), 14);
    }

    private void testConversions(Position position, int index) {
        assertThat(position.toIndex(5, 3), is(index));
        assertThat(Position.fromIndex(index, 5, 3), is(position.wrap(5, 3)));
    }

}
