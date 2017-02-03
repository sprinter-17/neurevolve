package neurevolve.world;

public class Time {

    private static final String[] SEASON_NAMES = {"Winter", "Spring", "Summer", "Autumn"};

    private final WorldConfiguration config;
    private int tickCount = 0;

    public Time(WorldConfiguration config) {
        this.config = config;
    }

    /**
     * Get the current time.
     *
     * @return the number of times {@link #tick} has been invoked.
     */
    public int getTime() {
        return tickCount;
    }

    /**
     * Get the name of the current season as defined by the year length
     *
     * @return the name of the season
     */
    public String getSeasonName() {
        int season = 4 * timeOfYear() / config.getYearLength();
        return SEASON_NAMES[season];
    }

    /**
     * Get the temperature variation for the current time of year. The maximum variation is at the
     * start and end of the year and 0 variation in the middle of the year. Other values are
     * extrapolated between these extremes.
     *
     * @return the variation in temperature at the current time of year
     */
    public int getSeasonalTemp() {
        int timeFromMidYear = Math.abs(config.getYearLength() / 2 - timeOfYear());
        return config.getTempVariation() * 2 * timeFromMidYear / config.getYearLength();
    }

    /**
     * Get the time within the current year
     */
    private int timeOfYear() {
        return tickCount % config.getYearLength();
    }

    /**
     * Increase the time by 1
     */
    public void tick() {
        tickCount++;
    }
}
