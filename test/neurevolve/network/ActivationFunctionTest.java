package neurevolve.network;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class ActivationFunctionTest {

    private SigmoidFunction function;

    @Before
    public void setup() {
        function = new SigmoidFunction(1000);
    }

    @Test
    public void testMidpoint() {
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
        assertTrue(function.apply(200) > 200);
        assertTrue(function.apply(400) > 400);
        assertTrue(function.apply(600) > 600);
        assertTrue(function.apply(-200) < -200);
        assertTrue(function.apply(-400) < -400);
        assertTrue(function.apply(-600) < -600);
    }

    public static void main(String[] arguments) {
        printGraph(new SigmoidFunction(50));
    }

    private static void printGraph(SigmoidFunction function) {
        for (int i = -55; i <= 55; i++) {
            int value = function.apply(i);
            System.out.print(String.format("%+03d  ", i));
            for (int j = -50; j <= value; j++) {
                System.out.print("*");
            }
            System.out.println();
        }
    }

}
