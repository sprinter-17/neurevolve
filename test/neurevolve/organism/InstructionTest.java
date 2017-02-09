package neurevolve.organism;

import java.util.ArrayDeque;
import java.util.Queue;
import neurevolve.TestEnvironment;
import neurevolve.network.Neuron;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class InstructionTest {

    private TestEnvironment environment;
    private Organism organism;
    private Queue<Integer> values;

    @Before
    public void setup() {
        environment = new TestEnvironment();
        organism = new Organism(environment, 100);
        values = new ArrayDeque<>();
    }

    @Test
    public void testAddNeuron() {
        Instruction.ADD_NEURON.complete(organism, values);
        assertThat(organism.getBrain().size(), is(1));
    }

    @Test
    public void testSetThreshold() {
        values.add(-27);
        Instruction.ADD_NEURON.complete(organism, values);
        organism.getBrain().activate();
        assertThat(organism.getBrain().getValue(0), is(27));
        assertTrue(values.isEmpty());
    }

    @Test
    public void testAddLink() {
        values.add(-27);
        Instruction.ADD_NEURON.complete(organism, values);
        Instruction.ADD_NEURON.complete(organism, values);

        values.add(0);
        values.add(20 * Neuron.WEIGHT_DIVISOR);
        Instruction.ADD_LINK.complete(organism, values);

        organism.getBrain().activate();
        assertThat(organism.getBrain().getValue(1), is(540));
    }

    @Test
    public void testAddInput() {
        Instruction.ADD_NEURON.complete(organism, values);

        values.add(5);
        values.add(5 * Neuron.WEIGHT_DIVISOR);
        Instruction.ADD_INPUT.complete(organism, values);

        organism.getBrain().activate();
        assertThat(organism.getBrain().getValue(0), is(250));
    }
}
