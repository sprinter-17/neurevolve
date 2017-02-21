package neurevolve.organism;

import static neurevolve.organism.Code.abs;
import static neurevolve.organism.Code.fromInt;
import static neurevolve.organism.Code.mod;
import static neurevolve.organism.Code.toInt;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class CodeTest {

    @Test
    public void testAbs() {
        assertThat(toInt(abs(fromInt(0))), is(0));
        assertThat(toInt(abs(fromInt(10))), is(10));
        assertThat(toInt(abs(fromInt(-10))), is(10));
    }

    @Test
    public void testMod() {
        assertThat(mod(fromInt(-10), 4), is(2));
    }
}
