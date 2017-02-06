package neurevolve.world;

import java.util.EnumMap;
import java.util.prefs.Preferences;

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

    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(WorldConfiguration.class);

    private enum Key {
        MUTATION_RATE("Mutation Rate", 1),
        MIN_TEMP("Min Temp", 100),
        MAX_TEMP("Max Temp", 120),
        YEAR_LENGTH("Year Length", 500),
        TEMP_VARIATION("Temp Variation", -100),
        MAX_RESOURCES("Max Resources", 500),
        SEED_COUNT("Seed Count", 200),
        INITIAL_ENERGY("Initial Energy", 1000),
        AGING_RATE("Aging Rate", 10),
        CONSUMPTION_RATE("Consumption Rate", 50),
        SIZE_RATE("Size Rate", 5),
        ACTIVITY("Activity", 1);

        private final String name;
        private final int defaultValue;

        private Key(String name, int defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }

        public int getValue(WorldConfiguration config) {
            return config.values.getOrDefault(this, defaultValue);
        }

        public void setValue(WorldConfiguration config, int value) {
            config.values.put(this, value);
        }
    }

    private final EnumMap<Key, Integer> values = new EnumMap<>(Key.class);
    private final EnumMap<WorldActivity, Integer> costs = new EnumMap<>(WorldActivity.class);

    public WorldConfiguration() {
        for (Key key : Key.values()) {
            values.put(key, PREFERENCES.getInt(key.name, key.defaultValue));
        }
        for (WorldActivity activity : WorldActivity.values()) {
            costs.put(activity, PREFERENCES.getInt(activityKey(activity), Key.ACTIVITY.defaultValue));
        }
    }

    public void write() {
        values.forEach((key, value) -> PREFERENCES.putInt(key.name, value));
        costs.forEach((act, value) -> PREFERENCES.putInt(activityKey(act), value));
    }

    private String activityKey(WorldActivity activity) {
        return Key.ACTIVITY.name + activity.name();
    }

    public int getMutationRate() {
        return Key.MUTATION_RATE.getValue(this);
    }

    /**
     * Set the mutation rate for the world. The mutation rate determines the likelihood of
     * transcription errors when copying a recipe. A mutation rate of 0 means that no errors occur.
     * A mutation rate of 1000 means that errors occur on every transcription.
     *
     * @param mutationRate the rate of mutation (range 0-1000)
     * @throws IllegalArgumentException if <tt>rate &lt; 0 || rate &gt; 1000</tt>
     */
    public void setMutationRate(int mutationRate) {
        if (mutationRate < 0 || mutationRate > 1000)
            throw new IllegalArgumentException("Mutation rate must be in the range 0-1000");
        Key.MUTATION_RATE.setValue(this, mutationRate);
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
        Key.YEAR_LENGTH.setValue(this, yearLength);;
        Key.TEMP_VARIATION.setValue(this, tempVariation);
    }

    public int getSeedCount() {
        return Key.SEED_COUNT.getValue(this);
    }

    public void setSeedCount(int count) {
        Key.SEED_COUNT.setValue(this, count);
    }

    public int getInitialEnergy() {
        return Key.INITIAL_ENERGY.getValue(this);
    }

    public void setInitialEnergy(int energy) {
        Key.INITIAL_ENERGY.setValue(this, energy);
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

    public int getMaxResources() {
        return Key.MAX_RESOURCES.getValue(this);
    }

    public void setMaxResources(int resources) {
        if (resources < 1)
            throw new IllegalArgumentException("Max resources must be greater than 0");
        Key.MAX_RESOURCES.setValue(this, resources);
    }

    public int getActivityCost(WorldActivity activity) {
        return costs.getOrDefault(activity, Key.ACTIVITY.defaultValue);
    }

    public void setActivityCost(WorldActivity activity, int cost) {
        costs.put(activity, cost);
    }

    public int getAgingRate() {
        return Key.AGING_RATE.getValue(this);
    }

    public void setAgingRate(int rate) {
        Key.AGING_RATE.setValue(this, rate);
    }

    public int getSizeRate() {
        return Key.SIZE_RATE.getValue(this);
    }

    public void setSizeRate(int rate) {
        Key.SIZE_RATE.setValue(this, rate);
    }
}
