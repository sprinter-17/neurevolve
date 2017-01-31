package neurevolve.world;

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

    private int mutationRate = 0;
    private int yearLength = 1;
    private int minTemp = 0;
    private int maxTemp = 0;
    private int tempVariation = 0;
    private int consumptionRate = 40;

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
        this.mutationRate = mutationRate;
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
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
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
        this.yearLength = yearLength;
        this.tempVariation = tempVariation;
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
        this.consumptionRate = consumptionRate;
    }

    public int getMutationRate() {
        return mutationRate;
    }

    public int getMinTemp() {
        return minTemp;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public int getYearLength() {
        return yearLength;
    }

    public int getTempVariation() {
        return tempVariation;
    }

    public int getConsumptionRate() {
        return consumptionRate;
    }

}
