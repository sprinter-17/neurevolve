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

    public class Neuron {

        private final int id;
        private final int threshold;
        private final Map<Integer, Integer> links = new HashMap<>();
        private final Map<Integer, Integer> inputs = new HashMap<>();
        private final List<Integer> outputs = new ArrayList<>();
        private Optional<Integer> activity = Optional.empty();
        private int delay = 0;

        public Neuron(int threshold) {
            this.id = neurons.size();
            this.threshold = threshold;
        }

        public int getThreshold() {
            return threshold;
        }

        public int getDelay() {
            return delay;
        }

        public void forEachLink(BiConsumer<Integer, Integer> action) {
            links.forEach(action);
        }

        public void forEachInput(BiConsumer<Integer, Integer> action) {
            inputs.forEach(action);
        }

        public boolean hasActivity() {
            return activity.isPresent();
        }

        public String getActivityName() {
            return environment.describeActivity(activity.get());
        }

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

        private String weight(int weight) {
            return String.format("%+d", weight);
        }
    }

    public int getSize() {
        return neurons.size();
    }

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
                last().ifPresent(n -> n.links.put(values[0], values[1]));
                last().ifPresent(n -> neurons.get(values[0]).outputs.add(n.id));
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

    private Optional<Neuron> last() {
        if (neurons.isEmpty())
            return Optional.empty();
        else
            return Optional.of(neurons.get(neurons.size() - 1));
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();
        description.append(IntStream.range(0, neurons.size())
                .mapToObj(i -> neurons.get(i).toString())
                .collect(Collectors.joining(" | ")));
        return description.toString();
    }
}
