package neurevolve.world;

public class Time {

    private static final String[] SEASON_NAMES = {"Spring", "Summer", "Autumn", "Winter"};

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
     * Get the name of the current season as defined by the year length and temp variation. If there
     * is no temp variation then the season is "None"
     *
     * @return the name of the season
     */
    public String getSeasonName() {
        if (config.getTempVariation() == 0 || config.getYearLength() < 4)
            return "None";
        int season = (7 + 8 * timeOfYear() / config.getYearLength()) / 2 % 4;
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
        int seasonLength = config.getYearLength() / 4;
        if (seasonLength == 0)
            return 0;
        int timeFromMidYear = Math.abs(config.getYearLength() / 2 - timeOfYear());
        int timeFromMidSeason = seasonLength - timeFromMidYear;
        return config.getTempVariation() * timeFromMidSeason / seasonLength;
    }

    /**
     * Get the time within the current year
     *
     * @return the tick within the year
     */
    public int timeOfYear() {
        return tickCount % config.getYearLength();
    }

    public int getYear() {
        return tickCount / config.getYearLength();
    }

    /**
     * Increase the time by 1
     */
    public void tick() {
        tickCount++;
    }
}
