package neurevolve.network;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
        neuron.addInput(() -> 17, 100);
        neuron.activate();
        assertThat(neuron.getValue(), is(17));
    }

    @Test
    public void testMultipleInputs() {
        neuron.addInput(() -> 11, 100);
        neuron.addInput(() -> -5, 100);
        neuron.activate();
        assertThat(neuron.getValue(), is(6));
    }

    @Test
    public void testInputsWithThreshold() {
        neuron.addInput(() -> 11, 100);
        neuron.addInput(() -> -5, 100);
        neuron.setThreshold(3);
        neuron.activate();
        assertThat(neuron.getValue(), is(3));
    }

    @Test
    public void testActivationFunction() {
        neuron = new Neuron(n -> -n);
        neuron.addInput(() -> 10, 100);
        neuron.activate();
        assertThat(neuron.getValue(), is(-10));
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
        verify(activity, never()).fire();
        neuron.setThreshold(-1);
        neuron.activate();
        verify(activity, times(1)).fire();
    }

}
