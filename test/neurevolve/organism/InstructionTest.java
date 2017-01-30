package neurevolve.organism;

import java.util.ArrayDeque;
import java.util.Queue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class InstructionTest {

    private Organism organism;
    private Queue<Integer> values;

    @Before
    public void setup() {
        organism = new Organism(n -> n, 100);
        values = new ArrayDeque<>();
    }

    @Test
    public void testAddNeuron() {
        Instruction.ADD_NEURON.complete(organism, values);
        assertThat(organism.size(), is(1));
    }

    @Test
    public void testSetThreshold() {
        values.add(-27);
        Instruction.ADD_NEURON.complete(organism, values);
        Instruction.SET_THRESHOLD.complete(organism, values);
        organism.getBrain().activate();
        assertThat(organism.getBrain().getValue(0), is(27));
        assertTrue(values.isEmpty());
    }

    @Test
    public void testAddLink() {
        values.add(-27);
        Instruction.ADD_NEURON.complete(organism, values);
        Instruction.SET_THRESHOLD.complete(organism, values);

        values.add(0);
        values.add(200);
        Instruction.ADD_NEURON.complete(organism, values);
        Instruction.ADD_LINK.complete(organism, values);

        organism.getBrain().activate();
        assertThat(organism.getBrain().getValue(1), is(54));
    }
}
