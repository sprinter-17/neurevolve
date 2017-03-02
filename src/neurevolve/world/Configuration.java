package neurevolve.world;

import java.util.EnumMap;
import neurevolve.organism.Code;
import neurevolve.organism.Instruction;
import neurevolve.organism.Recipe;

/**
 * Contains all the variables that can be changed through the simulation to adjust the behaviour of
 * the world.
 * <ul>
 * <li>mutation rate
 * <li>year length and temperature variation
 * <li>temperature range by latitude
 * </ul>
 */
public class Configuration {

    public enum Value {
        NORMAL_MUTATION_RATE(0, 10, 200),
        RADIATION_MUTATION_RATE(0, 100, 500),
        ACID_TOXICITY(0, 50, 500),
        MIN_TEMP(-200, 100, 200),
        MAX_TEMP(-200, 120, 200),
        YEAR_LENGTH(1, 500, 2000),
        TEMP_VARIATION(0, 10, 100),
        SEED_COUNT(0, 200, 1000),
        INITIAL_ENERGY(0, 200, 1000),
        MAX_ENERGY(50, 5000, 5000),
        MIN_SPLIT_TIME(0, 10, 100),
        MIN_SPLIT_ENERGY(0, 50, 500),
        BASE_COST(0, 1, 10),
        AGING_RATE(0, 10, 20),
        CONSUMPTION_RATE(0, 50, 100),
        SIZE_RATE(0, 5, 10),
        ACTIVITY_COST(0, 1, 40),
        ACTIVITY_FACTOR(0, 50, 200),
        HALF_LIFE(0, 1000, 1000);

        private final int defaultValue;
        private final int minValue;
        private final int maxValue;

        private Value(int minValue, int defaultValue, int maxValue) {
            this.defaultValue = defaultValue;
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        public int getMin() {
            return minValue;
        }

        public int getDefault() {
            return defaultValue;
        }

        public int getMax() {
            return maxValue;
        }
    }

    private final EnumMap<Value, Integer> values = new EnumMap<>(Value.class);
    private final EnumMap<WorldActivity, Integer> costs = new EnumMap<>(WorldActivity.class);
    private final EnumMap<WorldActivity, Integer> factors = new EnumMap<>(WorldActivity.class);
    private final EnumMap<GroundElement, Integer> halfLives = new EnumMap<>(GroundElement.class);
    private Recipe seedRecipe;

    {
        halfLives.put(GroundElement.BODY, 4);
    }

    public Configuration() {
        seedRecipe = new Recipe(0);
        seedRecipe.add(Instruction.ADD_NEURON, Code.fromInt(0));
        seedRecipe.add(Instruction.SET_ACTIVITY, WorldActivity.EAT_HERE.code());
        seedRecipe.add(Instruction.ADD_NEURON, Code.fromInt(0));
        seedRecipe.add(Instruction.SET_ACTIVITY, WorldActivity.DIVIDE.code());
    }

    public int getValue(Value value) {
        return values.getOrDefault(value, value.defaultValue);
    }

    public void setValue(Value value, int amount) {
        if (amount < value.minValue)
            throw new IllegalArgumentException("Value is smaller than minimum for " + value.name());
        if (amount > value.maxValue)
            throw new IllegalArgumentException("Value is larger than maximum for " + value.name());
        values.put(value, amount);
    }

    public void setSeedRecipe(Recipe recipe) {
        this.seedRecipe = recipe;
    }

    public Recipe getSeedRecipe() {
        return seedRecipe;
    }

    public int getNormalMutationRate() {
        return getValue(Value.NORMAL_MUTATION_RATE);
    }

    /**
     * Set the mutation rate for the world. The mutation rate determines the likelihood of
     * transcription errors when copying a recipe. A mutation rate of 0 means that no errors occur.
     * A mutation rate of 1000 means that errors occur on every transcription.
     *
     * @param rate the rate of mutation (range 0-1000)
     * @throws IllegalArgumentException if <tt>rate &lt; 0 || rate &gt; 1000</tt>
     */
    public void setNormalMutationRate(int rate) {
        if (rate < 0 || rate > 1000)
            throw new IllegalArgumentException("Mutation rate must be in the range 0-1000");
        setValue(Value.NORMAL_MUTATION_RATE, rate);
    }

    public int getRadiatedMutationRate() {
        return getValue(Value.RADIATION_MUTATION_RATE);
    }

    public void setRadiatedMutationRate(int rate) {
        if (rate < 0 || rate > 1000)
            throw new IllegalArgumentException("Radiated mutations rate must be in the range 0=1000");
        setValue(Value.RADIATION_MUTATION_RATE, rate);
    }

    public int getAcidToxicity() {
        return getValue(Value.ACID_TOXICITY);
    }

    public void setAcidToxicity(int toxicity) {
        setValue(Value.ACID_TOXICITY, toxicity);
    }

    public int getMinTemp() {
        return getValue(Value.MIN_TEMP);
    }

    public int getMaxTemp() {
        return getValue(Value.MAX_TEMP);
    }

    /**
     * Set the range of temperatures in the world. The temperature is determined by the vertical
     * distance of a position which range between the minimum temperature along the bottom and top
     * edges and the maximum temperature in the centre.
     *
     * @param minTemp the temperature along the top and bottom edges
     * @param maxTemp the temperature in the centre of the frame
     * @throws IllegalArgumentException if <tt>minTemp &gt; maxTemp</tt>
     */
    public void setTemperatureRange(int minTemp, int maxTemp) {
        if (minTemp > maxTemp)
            throw new IllegalArgumentException("Minimum temperature must be less than maximum temperature");
        setValue(Value.MIN_TEMP, minTemp);
        setValue(Value.MAX_TEMP, maxTemp);
    }

    public int getYearLength() {
        return getValue(Value.YEAR_LENGTH);
    }

    public int getTempVariation() {
        return getValue(Value.TEMP_VARIATION);
    }

    /**
     * Set the temperature variation that occurs through a yearly cycle. The difference to the
     * standard temperature varies smoothly according to the position within the cycle with no
     * variation at the start/end and maximum variation in the centre of the cycle.
     *
     * @param yearLength the total link (in ticks) of a complete temperature cycle
     * @param tempVariation the maximum variation (positive or negative) in temperature
     * @throws IllegalArgumentException if the year is not positive
     */
    public void setYear(int yearLength, int tempVariation) {
        if (yearLength < 1)
            throw new IllegalArgumentException("Year length must be positive");
        if (tempVariation < 0)
            throw new IllegalArgumentException("Temperature variation must not be negative");
        setValue(Value.YEAR_LENGTH, yearLength);
        setValue(Value.TEMP_VARIATION, tempVariation);
    }

    public int getSeedCount() {
        return getValue(Value.SEED_COUNT);
    }

    public int getSeedInitialEnergy() {
        return getValue(Value.INITIAL_ENERGY);
    }

    public void setSeed(int count, int energy) {
        setValue(Value.SEED_COUNT, count);
        setValue(Value.INITIAL_ENERGY, energy);
    }

    public int getDefaultActivityCost() {
        return getValue(Value.ACTIVITY_COST);
    }

    public void setDefaultActivityCost(int cost) {
        setValue(Value.ACTIVITY_COST, cost);
    }

    public int getDefaultActivityFactor() {
        return getValue(Value.ACTIVITY_FACTOR);
    }

    public void setDefaultActivityFactor(int factor) {
        setValue(Value.ACTIVITY_FACTOR, factor);
    }

    public int getActivityCost(WorldActivity activity) {
        return costs.getOrDefault(activity, getDefaultActivityCost());
    }

    public void setActivityCost(WorldActivity activity, int cost) {
        costs.put(activity, cost);
    }

    public int getActivityFactor(WorldActivity activity) {
        return factors.getOrDefault(activity, getDefaultActivityFactor());
    }

    public void setActivityFactor(WorldActivity activity, int factor) {
        factors.put(activity, factor);
    }

    public int getHalfLife(GroundElement element) {
        return halfLives.getOrDefault(element, Value.HALF_LIFE.defaultValue);
    }

    public void setHalfLife(GroundElement element, int period) {
        halfLives.put(element, period);
    }

}
