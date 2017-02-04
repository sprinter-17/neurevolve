package neurevolve.network;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NeuronTest {

    private Neuron neuron;

    @Before
    public void setup() {
        neuron = new Neuron(n -> n);
    }

    @Test
    public void testDefaultNeuron() {
        assertThat(neuron.getValue(), is(0));
    }

    @Test
    public void testThreshold() {
        neuron.setThreshold(-20);
        neuron.activate();
        assertThat(neuron.getValue(), is(20));
    }

    @Test
    public void testSingleInput() {
        neuron.addInput(() -> 17, 10);
        neuron.activate();
        assertThat(neuron.getValue(), is(170));
    }

    @Test
    public void testMultipleInputs() {
        neuron.addInput(() -> 11, 10);
        neuron.addInput(() -> -5, 10);
        neuron.activate();
        assertThat(neuron.getValue(), is(60));
    }

    @Test
    public void testInputsWithThreshold() {
        neuron.addInput(() -> 11, 10);
        neuron.addInput(() -> -5, 10);
        neuron.setThreshold(3);
        neuron.activate();
        assertThat(neuron.getValue(), is(57));
    }

    @Test
    public void testActivationFunction() {
        neuron = new Neuron(n -> -n);
        neuron.addInput(() -> 10, 10);
        neuron.activate();
        assertThat(neuron.getValue(), is(-100));
    }

    @Test
    public void testValueOnlySetAtActivation() {
        neuron.setThreshold(17);
        neuron.activate();
        neuron.setThreshold(13);
        assertThat(neuron.getValue(), is(-17));
    }

    @Test
    public void testActivityTriggeredAtActivation() {
        Activity activity = mock(Activity.class);
        neuron.setActivity(activity);
        neuron.setThreshold(1);
        neuron.activate();
        verify(activity, never()).perform();
        neuron.setThreshold(-1);
        neuron.activate();
        verify(activity, times(1)).perform();
    }

    @Test
    public void testMemory() {
        Input input = mock(Input.class);
        when(input.getValue()).thenReturn(7);
        neuron.addInput(input, 1);
        neuron.addDelay(2);
        neuron.activate();
        assertThat(neuron.getValue(), is(0));
        neuron.activate();
        assertThat(neuron.getValue(), is(0));
        neuron.activate();
        assertThat(neuron.getValue(), is(7));
    }

}
