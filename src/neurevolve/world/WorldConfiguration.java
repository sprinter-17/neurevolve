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
public class WorldConfiguration {

    public enum Key {
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

        private Key(int minValue, int defaultValue, int maxValue) {
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

        public int getValue(WorldConfiguration config) {
            return config.values.getOrDefault(this, defaultValue);
        }

        public void setValue(WorldConfiguration config, int value) {
            if (value < minValue)
                throw new IllegalArgumentException("Value is smaller than minimum for " + name());
            if (value > maxValue)
                throw new IllegalArgumentException("Value is larger than maximum for " + name());
            config.values.put(this, value);
        }
    }

    private final EnumMap<Key, Integer> values = new EnumMap<>(Key.class);
    private final EnumMap<WorldActivity, Integer> costs = new EnumMap<>(WorldActivity.class);
    private final EnumMap<WorldActivity, Integer> factors = new EnumMap<>(WorldActivity.class);
    private final EnumMap<GroundElement, Integer> halfLives = new EnumMap<>(GroundElement.class);
    private Recipe seedRecipe;

    {
        halfLives.put(GroundElement.BODY, 4);
    }

    public WorldConfiguration() {
        seedRecipe = new Recipe(0);
        seedRecipe.add(Instruction.ADD_NEURON, Code.fromInt(0));
        seedRecipe.add(Instruction.SET_ACTIVITY, WorldActivity.EAT_HERE.code());
        seedRecipe.add(Instruction.ADD_NEURON, Code.fromInt(0));
        seedRecipe.add(Instruction.SET_ACTIVITY, WorldActivity.DIVIDE.code());
    }

    public void setSeedRecipe(Recipe recipe) {
        this.seedRecipe = recipe;
    }

    public Recipe getSeedRecipe() {
        return seedRecipe;
    }

    public int getNormalMutationRate() {
        return Key.NORMAL_MUTATION_RATE.getValue(this);
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
        Key.NORMAL_MUTATION_RATE.setValue(this, rate);
    }

    public int getRadiatedMutationRate() {
        return Key.RADIATION_MUTATION_RATE.getValue(this);
    }

    public void setRadiatedMutationRate(int rate) {
        if (rate < 0 || rate > 1000)
            throw new IllegalArgumentException("Radiated mutations rate must be in the range 0=1000");
        Key.RADIATION_MUTATION_RATE.setValue(this, rate);
    }

    public int getAcidToxicity() {
        return Key.ACID_TOXICITY.getValue(this);
    }

    public void setAcidToxicity(int toxicity) {
        Key.ACID_TOXICITY.setValue(this, toxicity);
    }

    public int getMinTemp() {
        return Key.MIN_TEMP.getValue(this);
    }

    public int getMaxTemp() {
        return Key.MAX_TEMP.getValue(this);
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
        Key.MIN_TEMP.setValue(this, minTemp);
        Key.MAX_TEMP.setValue(this, maxTemp);
    }

    public int getYearLength() {
        return Key.YEAR_LENGTH.getValue(this);
    }

    public int getTempVariation() {
        return Key.TEMP_VARIATION.getValue(this);
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
        Key.YEAR_LENGTH.setValue(this, yearLength);;
        Key.TEMP_VARIATION.setValue(this, tempVariation);
    }

    public int getSeedCount() {
        return Key.SEED_COUNT.getValue(this);
    }

    public int getSeedInitialEnergy() {
        return Key.INITIAL_ENERGY.getValue(this);
    }

    public void setSeed(int count, int energy) {
        Key.SEED_COUNT.setValue(this, count);
        Key.INITIAL_ENERGY.setValue(this, energy);
    }

    public int getMinimumSplitTime() {
        return Key.MIN_SPLIT_TIME.getValue(this);
    }

    public void setMinimumSplitTime(int time) {
        Key.MIN_SPLIT_TIME.setValue(this, time);
    }

    public int getMinimumSplitEnergy() {
        return Key.MIN_SPLIT_ENERGY.getValue(this);
    }

    public void setMinimumSplitEnergy(int energy) {
        Key.MIN_SPLIT_ENERGY.setValue(this, energy);
    }

    public int getConsumptionRate() {
        return Key.CONSUMPTION_RATE.getValue(this);
    }

    /**
     * Set the rate at which organisms consume resources.
     *
     * @param consumptionRate the consumption rate
     * @throws IllegalArgumentException if <tt>consumptionRate &lt; 1</tt>
     */
    public void setConsumptionRate(int consumptionRate) {
        if (consumptionRate < 1)
            throw new IllegalArgumentException("Consumption rate must be greater than 0");
        Key.CONSUMPTION_RATE.setValue(this, consumptionRate);
    }

    public int getBaseCost() {
        return Key.BASE_COST.getValue(this);
    }

    public void setBaseCost(int cost) {
        if (cost < 0)
            throw new IllegalArgumentException("Base cost must be greater than or equal to 0");
        Key.BASE_COST.setValue(this, cost);
    }

    public int getDefaultActivityCost() {
        return Key.ACTIVITY_COST.getValue(this);
    }

    public void setDefaultActivityCost(int cost) {
        Key.ACTIVITY_COST.setValue(this, cost);
    }

    public int getDefaultActivityFactor() {
        return Key.ACTIVITY_FACTOR.getValue(this);
    }

    public void setDefaultActivityFactor(int factor) {
        Key.ACTIVITY_FACTOR.setValue(this, factor);
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

    public int getAgeCost() {
        return Key.AGING_RATE.getValue(this);
    }

    public void setAgeCost(int cost) {
        Key.AGING_RATE.setValue(this, cost);
    }

    public int getSizeCost() {
        return Key.SIZE_RATE.getValue(this);
    }

    public void setSizeCost(int cost) {
        Key.SIZE_RATE.setValue(this, cost);
    }

    public int getHalfLife(GroundElement element) {
        return halfLives.getOrDefault(element, Key.HALF_LIFE.defaultValue);
    }

    public void setHalfLife(GroundElement element, int period) {
        halfLives.put(element, period);
    }

}
