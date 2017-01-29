package neurevolve.network;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class ActivationFunctionTest {

    private ActivationFunction function;

    @Before
    public void setup() {
        function = new ActivationFunction(1000);
    }

    @Test
    public void testOrigin() {
        assertThat(function.apply(0), is(0));
    }

    @Test
    public void testUpperLimit() {
        assertThat(function.apply(1000), is(1000));
        assertThat(function.apply(1001), is(1000));
        assertThat(function.apply(Integer.MAX_VALUE), is(1000));
    }

    @Test
    public void testLowerLimit() {
        assertThat(function.apply(-1000), is(-1000));
        assertThat(function.apply(-1001), is(-1000));
        assertThat(function.apply(Integer.MIN_VALUE), is(-1000));
    }

    @Test
    public void testSigmoid() {
        assertTrue(function.apply(200) < 200);
        assertTrue(function.apply(900) > 900);
    }

    public static void main(String[] arguments) {
        printGraph(new ActivationFunction(50));
    }

    private static void printGraph(ActivationFunction function) {
        for (int i = -60; i <= 60; i++) {
            int value = function.apply(i);
            System.out.print(String.format("%+03d  ", i));
            for (int j = -50; j <= value; j++) {
                System.out.print("*");
            }
            System.out.println();
        }
    }

}
