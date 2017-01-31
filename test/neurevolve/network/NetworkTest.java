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

public class NetworkTest {

    private Network network;

    @Before
    public void setup() {
        network = new Network(n -> n);
    }

    @Test
    public void testEmptyNetwork() {
        assertThat(network.size(), is(0));
    }

    @Test
    public void testAddNeuron() {
        network.addNeuron();
        assertThat(network.size(), is(1));
        network.addNeuron();
        assertThat(network.size(), is(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testNeuronIndexOutOfBounds() {
        network.getValue(0);
    }

    @Test
    public void testGetValue() {
        network.addNeuron();
        assertThat(network.getValue(0), is(0));
    }

    @Test
    public void testSetThreshold() {
        network.addNeuron();
        network.setThreshold(-10);
        network.activate();
        assertThat(network.getValue(0), is(10));
        network.setThreshold(11);
        network.activate();
        assertThat(network.getValue(0), is(-11));
    }

    @Test
    public void testAddInput() {
        Input input = mock(Input.class);
        network.addNeuron();
        network.addInput(input, 10);
        when(input.getValue()).thenReturn(17);
        network.activate();
        assertThat(network.getValue(0), is(17));
        when(input.getValue()).thenReturn(14);
        network.activate();
        assertThat(network.getValue(0), is(14));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddLinkToSelf() {
        network.addNeuron();
        network.addLink(0, 100);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddLinkToForwardNeuron() {
        network.addNeuron();
        network.addNeuron();
        network.addLink(2, 100);
    }

    @Test
    public void testAddLink() {
        network.addNeuron();
        network.setThreshold(-28);
        network.addNeuron();
        network.addLink(0, 10);
        network.activate();
        assertThat(network.getValue(1), is(28));
    }

    @Test
    public void testSetActivity() {
        Activity activity = mock(Activity.class);
        network.addNeuron();
        network.setActivity(activity);
        network.setThreshold(-1);
        network.activate();
        verify(activity).perform();
    }

    @Test
    public void testThreshold() {
        Activity activity = mock(Activity.class);
        network.addNeuron();
        network.setActivity(activity);
        network.setThreshold(1);
        network.activate();
        verify(activity, never()).perform();
        network.setThreshold(0);
        network.activate();
        verify(activity, times(1)).perform();
    }

}
