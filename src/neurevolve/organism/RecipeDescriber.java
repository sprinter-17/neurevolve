package neurevolve.organism;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The function of a <code>RecipeDescriber</code> is to turn a {@link Recipe} into information about
 * the recipe that can be displayed to the user. It is designed to ignore all instructions in the
 * recipe that have no effect on the constructed network. It also allows neurons that
 */
public class RecipeDescriber {

    private final Environment environment;
    private final List<Neuron> neurons = new ArrayList<>();

    /**
     * A <code>RecipeDescriber</code> contains a list of {@code Neuron} objects that contain
     * information about each of the neurons created by this recipe.
     */
    public class Neuron {

        private final int id;
        private final int threshold;
        private final Map<Integer, Integer> links = new HashMap<>();
        private final Map<Integer, Integer> inputs = new HashMap<>();
        private final List<Integer> outputs = new ArrayList<>();
        private Optional<Integer> activity = Optional.empty();
        private int delay = 0;

        /**
         * Construct a Neuron
         *
         * @param threshold the threshold for the neuron
         */
        public Neuron(int threshold) {
            this.id = neurons.size();
            this.threshold = threshold;
        }

        /**
         * @return the neuron's threshold
         */
        public int getThreshold() {
            return threshold;
        }

        /**
         * @return the neuron's delay
         */
        public int getDelay() {
            return delay;
        }

        /**
         * Perform an operation for each of the neuron's links to previous neurons.
         *
         * @param action the operation to perform
         */
        public void forEachLink(BiConsumer<Integer, Integer> action) {
            links.forEach(action);
        }

        /**
         * Perform an operation for each input to the neuron
         *
         * @param action the operation to perform
         */
        public void forEachInput(BiConsumer<Integer, Integer> action) {
            inputs.forEach(action);
        }

        /**
         * @return true, if the neuron has an activity
         */
        public boolean hasActivity() {
            return activity.isPresent();
        }

        /**
         * Get the name of the activity for this neuron, if any.
         *
         * @return the activity's name
         */
        public String getActivityName() {
            return environment.describeActivity(activity.get());
        }

        public boolean isInactive() {
            return !hasActivity() && outputsAreUseless();
        }

        private boolean outputsAreUseless() {
            return outputs.stream().map(neurons::get).allMatch(Neuron::isInactive);
        }

        /**
         * Generate a human-readable description of the neuron including weight, delay, inputs,
         * outputs and activity (if any).
         *
         * @return the description.
         */
        @Override
        public String toString() {
            StringBuilder description = new StringBuilder();
            description.append("N").append(id + 1).append(weight(threshold));
            if (delay > 0)
                description.append("d").append(delay);
            if (hasActivity())
                description.append(" ").append(getActivityName());
            links.forEach((l, w) -> description.append(" <N").append(l + 1).append(weight(w)));
            inputs.forEach((i, w) -> description.append(" ").append(environment.describeInput(i))
                    .append(weight(w)));
            outputs.forEach(o -> description.append(" >N").append(o + 1));
            return description.toString();
        }

        /**
         * Describe a weight value.
         *
         * @param weight the value to describe
         * @return the description
         */
        private String weight(int weight) {
            return String.format("%+d", weight);
        }
    }

    /**
     * @return the number of neurons in networks generated by this recipe
     */
    public int getSize() {
        return neurons.size();
    }

    /**
     * Get the neuron at a given position.
     *
     * @param index the position of the neuron
     * @return a description of a neuron
     */
    public Neuron getNeuron(int index) {
        return neurons.get(index);
    }

    /**
     * Construct a <code>RecipeDescriber</code> from a recipe and environment.
     *
     * @param recipe the recipe to describe
     * @param environment the environment to use to describe inputs and activities
     */
    public RecipeDescriber(Recipe recipe, Environment environment) {
        this.environment = environment;
        recipe.forEachInstruction(this::process);
    }

    private void process(Instruction instruction, int... values) {
        switch (instruction) {
            case ADD_NEURON:
                neurons.add(new Neuron(values[0]));
                break;
            case ADD_LINK:
                if (neurons.size() > 1) {
                    int from = Math.floorMod(values[0], neurons.size() - 1);
                    last().ifPresent(n -> n.links.put(from, values[1]));
                    last().ifPresent(n -> neurons.get(from).outputs.add(n.id));
                }
                break;
            case ADD_INPUT:
                last().ifPresent(n -> n.inputs.put(values[0], values[1]));
                break;
            case ADD_DELAY:
                last().ifPresent(n -> n.delay = values[0]);
                break;
            case SET_ACTIVITY:
                last().ifPresent(n -> n.activity = Optional.of(values[0]));
                break;
            default:
                throw new AssertionError(instruction.name());
        }
    }

    /**
     * Get the last neuron to be created
     */
    private Optional<Neuron> last() {
        if (neurons.isEmpty())
            return Optional.empty();
        else
            return Optional.of(neurons.get(neurons.size() - 1));
    }

    /**
     * Convert the recipe description to human-readable text.
     *
     * @return the description of the recipe
     */
    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();
        description.append(IntStream.range(0, neurons.size())
                .mapToObj(i -> neurons.get(i).toString())
                .collect(Collectors.joining(" | ")));
        return description.toString();
    }
}
