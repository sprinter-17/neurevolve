package neurevolve.organism;

import neurevolve.TestEnvironment;
import neurevolve.network.Neuron;
import static neurevolve.organism.Code.fromInt;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class InstructionTest {

    private TestEnvironment environment;
    private Organism organism;

    @Before
    public void setup() {
        environment = new TestEnvironment();
        organism = new Organism(environment, 100);
    }

    @Test
    public void testAddNeuron() {
        Instruction.ADD_NEURON.complete(organism, Code.fromInt(4));
        assertThat(organism.getBrain().size(), is(1));
    }

    @Test
    public void testSetThreshold() {
        Instruction.ADD_NEURON.complete(organism, Code.fromInt(-27));
        organism.getBrain().activate();
        assertThat(organism.getBrain().getValue(0), is(27));
    }

    @Test
    public void testAddLink() {
        Instruction.ADD_NEURON.complete(organism, fromInt(-0));
        Instruction.ADD_NEURON.complete(organism, fromInt(-27));
        Instruction.ADD_NEURON.complete(organism, fromInt(0));
        Instruction.ADD_LINK.complete(organism, fromInt(1), fromInt(20 * Neuron.WEIGHT_DIVISOR));
        organism.getBrain().activate();
        assertThat(organism.getBrain().getValue(2), is(540));
    }

    @Test
    public void testAddInput() {
        Instruction.ADD_NEURON.complete(organism, fromInt(0));
        Instruction.ADD_INPUT.complete(organism, fromInt(5), fromInt(5 * Neuron.WEIGHT_DIVISOR));
        organism.getBrain().activate();
        assertThat(organism.getBrain().getValue(0), is(250));
    }
}
