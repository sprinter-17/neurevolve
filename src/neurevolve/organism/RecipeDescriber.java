package neurevolve.organism;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.stream.Stream;

/**
 * The function of a <code>RecipeDescriber</code> is to turn a {@link Recipe} into human-readable
 * text. It is designed to ignore all instructions in the recipe that have no effect on the
 * constructed network. It outputs the length of the recipe followed by a description of each of the
 * neurons. Only valid inputs, lengths and final activity set are shown so that the description
 * represents the network that the recipe constructs.
 */
public class RecipeDescriber {

    private final Environment environment;
    private final Queue<Integer> instructions;
    private final Deque<String> descriptions = new ArrayDeque<>();
    private int neuronCount = 0;
    private Optional<NeuronDescriber> neuron = Optional.empty();

    /**
     * Collect information about the current Neuron
     */
    private class NeuronDescriber {

        private final int id;
        private final int threshold;
        private OptionalInt activity = OptionalInt.empty();
        private final List<SynapseDescriber> synapses = new ArrayList<>();
        private final List<InputDescriber> inputs = new ArrayList<>();

        public NeuronDescriber(int threshold) {
            id = ++neuronCount;
            this.threshold = threshold;
        }

        public void describe() {
            StringBuilder description = new StringBuilder();
            description.append("N").append(id);
            describeWeight(description, threshold);
            inputs.forEach(i -> i.describe(description));
            synapses.stream()
                    .sorted(Comparator.comparingInt(s -> s.from))
                    .forEach(s -> s.describe(description));
            activity.ifPresent(act -> description
                    .append(" ")
                    .append(environment.describeActivity(act)));
            descriptions.add(description.toString());
        }
    }

    /**
     * Collect information about the current link
     */
    private class SynapseDescriber {

        private final int from;
        private final int weight;

        public SynapseDescriber(int from, int weight) {
            this.from = from;
            this.weight = weight;
        }

        public void describe(StringBuilder description) {
            description.append(" ^N").append(from);
            describeWeight(description, weight);
        }
    }

    /**
     * Collect information about the current input
     */
    private class InputDescriber {

        private final int input;
        private final int weight;

        public InputDescriber(int input, int weight) {
            this.input = input;
            this.weight = weight;
        }

        public void describe(StringBuilder description) {
            description.append(" ").append(environment.describeInput(input));
            describeWeight(description, weight);
        }
    }

    /**
     * Construct a <code>RecipeDescriber</code> from a recipe and environment.
     *
     * @param recipe the recipe to describe
     * @param environment the environment to use to describe inputs and activities
     */
    public RecipeDescriber(Recipe recipe, Environment environment) {
        this.instructions = recipe.instructionInQueue();
        this.environment = environment;
        describeRecipeLength();
        describeInstructions();
    }

    private void describeRecipeLength() {
        descriptions.add("Len" + instructions.size());
    }

    private void describeInstructions() {
        while (!instructions.isEmpty()) {
            switch (Instruction.decode(instructions.remove())) {
                case ADD_NEURON:
                    addNeuron();
                    break;
                case ADD_LINK:
                    if (neuron.isPresent())
                        addLink();
                    break;
                case ADD_INPUT:
                    if (neuron.isPresent())
                        addInput();
                    break;
                case SET_ACTIVITY:
                    if (neuron.isPresent())
                        setActivity();
                    break;
            }
        }
        neuron.ifPresent(NeuronDescriber::describe);
    }

    private void addNeuron() {
        neuron.ifPresent(NeuronDescriber::describe);
        int weight = value();
        neuron = Optional.of(new NeuronDescriber(weight));
    }

    private void addLink() {
        int from = value();
        if (from >= 0 && from < neuronCount - 1)
            neuron.get().synapses.add(new SynapseDescriber(from + 1, value()));
    }

    private void addInput() {
        neuron.get().inputs.add(new InputDescriber(value(), value()));
    }

    private void setActivity() {
        neuron.get().activity = OptionalInt.of(value());
    }

    /**
     * describe a weight
     */
    private void describeWeight(StringBuilder description, int weight) {
        description.append(String.format("%+d", weight));
    }

    /**
     * get a value from the queue of instructions
     */
    private int value() {
        if (instructions.isEmpty())
            return 0;
        else
            return instructions.remove();
    }

    /**
     * Get the description of the recipe.
     *
     * @return the description
     */
    public Stream<String> describe() {
        return descriptions.stream();
    }

}
