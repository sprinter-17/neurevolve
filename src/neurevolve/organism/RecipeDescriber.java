package neurevolve.organism;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.stream.Collectors;
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
    private final int length;
    private int junk = 0;
    private final List<NeuronDescription> descriptions = new ArrayList<>();
    private int neuronCount = 0;
    private Optional<NeuronDescriber> neuron = Optional.empty();

    public class NeuronDescription {

        private final StringBuilder neuron = new StringBuilder();
        private final List<StringBuilder> inputs = new ArrayList<>();
        private final List<StringBuilder> outputs = new ArrayList<>();
        private boolean hasActivity = false;

        public String getNeuronDescription() {
            return neuron.toString();
        }

        public boolean isNotJunk() {
            return hasActivity || !outputs.isEmpty();
        }

        public StringBuilder getInput() {
            StringBuilder input = new StringBuilder();
            inputs.add(input);
            return input;
        }

        public Stream<String> getInputDescriptions() {
            return inputs.stream().map(StringBuilder::toString);
        }

        public StringBuilder getOutput() {
            StringBuilder output = new StringBuilder();
            outputs.add(output);
            return output;
        }

        public Stream<String> getOutputDescriptions() {
            return outputs.stream().map(StringBuilder::toString);
        }

    }

    /**
     * Collect information about the current Neuron
     */
    private class NeuronDescriber {

        private final int id;
        private final int threshold;
        private int delay = 0;
        private OptionalInt activity = OptionalInt.empty();
        private final List<SynapseDescriber> synapses = new ArrayList<>();
        private final List<InputDescriber> inputs = new ArrayList<>();

        public NeuronDescriber(int threshold) {
            id = ++neuronCount;
            this.threshold = threshold;
        }

        public void describe() {
            NeuronDescription description = new NeuronDescription();
            description.neuron.append("N").append(id);
            describeWeight(description.neuron, threshold);
            if (delay > 0)
                description.neuron.append("d").append(delay);
            if (activity.isPresent()) {
                description.hasActivity = true;
                description.neuron
                        .append(" ")
                        .append(environment.describeActivity(activity.getAsInt()));
            }
            inputs.forEach(i -> i.describe(description.getInput()));
            synapses.stream()
                    .sorted(Comparator.comparingInt(s -> s.from))
                    .forEach(s -> s.describe(description.getInput()));
            descriptions.add(description);
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
            description.append(" <N").append(from);
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
        this.length = recipe.size();
        describeInstructions();
    }

    private void describeInstructions() {
        while (!instructions.isEmpty()) {
            switch (Instruction.decode(instructions.remove())) {
                case ADD_NEURON:
                    addNeuron();
                    break;
                case ADD_LINK:
                    ifNeuron(this::addLink);
                    break;
                case ADD_INPUT:
                    ifNeuron(this::addInput);
                    break;
                case ADD_DELAY:
                    ifNeuron(this::addDelay);
                    break;
                case SET_ACTIVITY:
                    ifNeuron(this::setActivity);
                    break;
            }
        }
        neuron.ifPresent(NeuronDescriber::describe);
    }

    private void ifNeuron(Runnable runnable) {
        if (neuron.isPresent())
            runnable.run();
        else
            junk++;
    }

    private void addNeuron() {
        neuron.ifPresent(NeuronDescriber::describe);
        int weight = value();
        neuron = Optional.of(new NeuronDescriber(weight));
    }

    private void addLink() {
        int from = value();
        if (from >= 0 && from < neuronCount - 1) {
            neuron.get().synapses.add(new SynapseDescriber(from + 1, value()));
            descriptions.get(from).getOutput().append(" >N").append(neuron.get().id);
        } else
            junk++;
    }

    private void addInput() {
        neuron.get().inputs.add(new InputDescriber(value(), value()));
    }

    private void addDelay() {
        int delay = value();
        if (delay > 0)
            neuron.get().delay += delay;
        else
            junk++;
    }

    private void setActivity() {
        if (neuron.get().activity.isPresent())
            junk++;
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
    public Stream<NeuronDescription> getNeuronDescriptions() {
        return descriptions.stream();
    }

    public String describe() {
        return describe(descriptions.stream().filter(NeuronDescription::isNotJunk));
    }

    public String describeAll() {
        return describe(descriptions.stream());
    }

    private String describe(Stream<NeuronDescription> descriptions) {
        return descriptions.map(nd -> nd.getNeuronDescription()
                + nd.getInputDescriptions().collect(Collectors.joining())
                + nd.getOutputDescriptions().collect(Collectors.joining()))
                .collect(Collectors.joining(" | "));
    }

    public int getLength() {
        return length;
    }

    public int getJunk() {
        return junk;
    }

}
