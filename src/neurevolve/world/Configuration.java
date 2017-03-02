package neurevolve.world;

import java.util.EnumMap;
import neurevolve.organism.Code;
import neurevolve.organism.Instruction;
import neurevolve.organism.Recipe;

/**
 * Contains all the variables that can be changed through the simulation to adjust the behaviour of
 * the world. Standard configuration items are contained in the {@link Value} enum. There are four
 * other non-standard configuration items that can be set:
 * <ul>
 * <li>Energy costs for an organism to perform an activity.</li>
 * <li>Factor applied to the energy costs for multiple activities of the same type.</li>
 * <li>Half lives of ground elements.</li>
 * <li>The recipe to use for new seed organisms in a world.</li>
 * </ul>
 */
public class Configuration {

    public enum Value {
        /**
         * The rate of copy errors during splits in positions that do not contain radiation.
         */
        NORMAL_MUTATION_RATE(0, 10, 200),
        /**
         * The rate of copy errors during splits in positions that do contain radiation.
         */
        RADIATION_MUTATION_RATE(0, 100, 500),
        /**
         * The amount of energy consumed by acid (per acid unit) while organisms occupy acidic
         * positions
         */
        ACID_TOXICITY(0, 50, 500),
        /**
         * The temperature of positions along the top and bottom edges of the world's space.
         */
        MIN_TEMP(-200, 100, 200),
        /**
         * The temperature of positions furthest from the top and bottom edges of the world's space.
         */
        MAX_TEMP(-200, 120, 200),
        /**
         * The number of ticks between each minimum temperature.
         */
        YEAR_LENGTH(1, 500, 2000),
        /**
         * The amount of temperature variation between the minimum and maximum temperatures in the
         * year.
         */
        TEMP_VARIATION(0, 10, 100),
        /**
         * The number of organisms to create while seeding the world.
         */
        SEED_COUNT(0, 200, 1000),
        /**
         * The initial energy of seed organisms.
         */
        INITIAL_ENERGY(0, 200, 1000),
        /**
         * The maximum amount of energy an organism can have. Attempts to consume resources beyond
         * this amount will fail, leaving the resources on the ground.
         */
        MAX_ENERGY(50, 5000, 5000),
        /**
         * The minimum number of ticks between splits for each organism.
         */
        MIN_SPLIT_TIME(0, 10, 100),
        /**
         * The minimum amount of energy an organism must have before it is able to split.
         */
        MIN_SPLIT_ENERGY(0, 50, 500),
        /**
         * A base energy cost for each organism each tick (before age, size and activity costs).
         */
        BASE_COST(0, 1, 10),
        /**
         * An energy cost for each organism each tick, per 100 ticks since creation.
         */
        AGING_RATE(0, 10, 20),
        /**
         * An energy cost for each organism each tick, per 10 neurons in the organism's network.
         */
        SIZE_RATE(0, 5, 10),
        /**
         * The maximum amount of resources that can be consumed by each consumption activity.
         */
        CONSUMPTION_RATE(0, 50, 100),
        /**
         * A default energy cost for all activities that do not have a specific energy cost. See
         * {@link #setActivityCost(neurevolve.world.WorldActivity, int)} for more details of
         * activity costs.
         */
        ACTIVITY_COST(0, 1, 40),
        /**
         * A default energy cost factor for all activities that do not have a specific energy cost
         * factor. See {@link #setActivityFactor(neurevolve.world.WorldActivity, int)} for more
         * details of energy cost factors.
         */
        ACTIVITY_FACTOR(0, 50, 200),
        /**
         * A default half life for all ground elements that do not have a specific half life. See
         * {@link #setHalfLife(neurevolve.world.GroundElement, int)} for more details of ground
         * element half lives.
         */
        HALF_LIFE(0, 1000, 1000);

        private final int defaultValue;
        private final int minValue;
        private final int maxValue;

        private Value(int minValue, int defaultValue, int maxValue) {
            this.defaultValue = defaultValue;
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        /**
         * @return the minimum allowed value for this configuration item.
         */
        public int getMin() {
            return minValue;
        }

        /**
         * @return the default value for this configuration item if no value is supplied.
         */
        public int getDefault() {
            return defaultValue;
        }

        /**
         * @return the maximum allowed value for this configuration item.
         */
        public int getMax() {
            return maxValue;
        }
    }

    private final EnumMap<Value, Integer> values = new EnumMap<>(Value.class);
    private final EnumMap<WorldActivity, Integer> costs = new EnumMap<>(WorldActivity.class);
    private final EnumMap<WorldActivity, Integer> factors = new EnumMap<>(WorldActivity.class);
    private final EnumMap<GroundElement, Integer> halfLives = new EnumMap<>(GroundElement.class);
    private Recipe seedRecipe;

    /**
     * Construct a {@code Configuration} to contain values.
     */
    public Configuration() {
        seedRecipe = new Recipe(0);
        seedRecipe.add(Instruction.ADD_NEURON, Code.fromInt(0));
        seedRecipe.add(Instruction.SET_ACTIVITY, WorldActivity.EAT_HERE.code());
        seedRecipe.add(Instruction.ADD_NEURON, Code.fromInt(0));
        seedRecipe.add(Instruction.SET_ACTIVITY, WorldActivity.DIVIDE.code());
        halfLives.put(GroundElement.BODY, 4);
    }

    /**
     * Get a configuration value.
     *
     * @param value the value to get.
     * @return the previously set value or the default for the value
     */
    public int getValue(Value value) {
        return values.getOrDefault(value, value.defaultValue);
    }

    /**
     * Set a configuration value.
     *
     * @param value the value to set.
     * @param amount the amount to set the value to.
     * @throws IllegalArgumentException if the amount is not within the value's range.
     */
    public void setValue(Value value, int amount) {
        if (amount < value.minValue)
            throw new IllegalArgumentException("Value is smaller than minimum for " + value.name());
        if (amount > value.maxValue)
            throw new IllegalArgumentException("Value is larger than maximum for " + value.name());
        values.put(value, amount);
    }

    /**
     * @return the recipe to use to create new organisms when seeding the world. If none is
     * specified, the default recipe is to create two neurons with zero threshold and
     * {@link neurevolve.world.WorldActivity#EAT_HERE} and
     * {@link neurevolve.world.WorldActivity#DIVIDE} activities.
     */
    public Recipe getSeedRecipe() {
        return seedRecipe;
    }

    /**
     * Specify the recipe to use to create new organisms when seeding the world.
     *
     * @param recipe the recipe to use to create seed organisms.
     */
    public void setSeedRecipe(Recipe recipe) {
        this.seedRecipe = recipe;
    }

    /**
     * Get the energy cost to perform a specific activity.
     *
     * @param activity the activity being performed
     * @return the cost of the activity or the current value of {@link Value#ACTIVITY_COST} if no
     * cost has been specified for this activity.
     */
    public int getActivityCost(WorldActivity activity) {
        return costs.getOrDefault(activity, getValue(Value.ACTIVITY_COST));
    }

    /**
     * Specify the cost of performing a specific activity.
     *
     * @param activity the activity whose cost is being specified
     * @param cost the cost of the activity
     */
    public void setActivityCost(WorldActivity activity, int cost) {
        costs.put(activity, cost);
    }

    public int getActivityFactor(WorldActivity activity) {
        return factors.getOrDefault(activity, getValue(Value.ACTIVITY_FACTOR));
    }

    public void setActivityFactor(WorldActivity activity, int factor) {
        factors.put(activity, factor);
    }

    /**
     * Get the half life of a given {@code GroundElement}. This is the number of ticks, on average,
     * before the number of units of the element at a position decreases by one.
     *
     * @param element the element whose half life is being retrieved.
     * @return the half life of the given element, or the current value of {@link Value#HALF_LIFE}.
     * For the element {@link neurevolve.world.GroundElement#BODY}, the default half life is 4.
     */
    public int getHalfLife(GroundElement element) {
        return halfLives.getOrDefault(element, getValue(Value.HALF_LIFE));
    }

    /**
     * Set the half life of a given element to a given period. The number of units of this element
     * will, on average, decrease by one in the number of ticks in the given period.
     *
     * @param element the element to specify a half life for
     * @param period the number of ticks
     */
    public void setHalfLife(GroundElement element, int period) {
        halfLives.put(element, period);
    }

}
