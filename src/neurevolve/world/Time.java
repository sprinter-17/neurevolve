package neurevolve.world;

import static neurevolve.world.Configuration.Value.TEMP_VARIATION;
import static neurevolve.world.Configuration.Value.YEAR_LENGTH;

public class Time {

    public enum Season {
        SPRING("Spring"),
        SUMMER("Summer"),
        AUTUMN("Autumn"),
        WINTER("Winter");

        private final String name;

        private Season(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Season valueOf(int time, int year) {
            return values()[(7 + 8 * (time % year) / year) / 2 % 4];
        }

    }

    private final Configuration config;
    private int tickCount = 0;

    public Time(Configuration config) {
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
        if (config.getValue(TEMP_VARIATION) == 0 || config.getValue(YEAR_LENGTH) < 4)
            return "None";
        return Season.valueOf(tickCount, config.getValue(YEAR_LENGTH)).getName();
    }

    /**
     * Get the temperature variation for the current time of year. The maximum variation is at the
     * start and end of the year and 0 variation in the middle of the year. Other values are
     * extrapolated between these extremes.
     *
     * @return the variation in temperature at the current time of year
     */
    public int getSeasonalTemp() {
        int seasonLength = config.getValue(YEAR_LENGTH) / 4;
        if (seasonLength == 0)
            return 0;
        int timeFromMidYear = Math.abs(config.getValue(YEAR_LENGTH) / 2 - timeOfYear());
        int timeFromMidSeason = seasonLength - timeFromMidYear;
        return config.getValue(TEMP_VARIATION) * timeFromMidSeason / seasonLength;
    }

    /**
     * Get the time within the current year
     *
     * @return the tick within the year
     */
    public int timeOfYear() {
        return tickCount % config.getValue(YEAR_LENGTH);
    }

    public int getYear() {
        return tickCount / config.getValue(YEAR_LENGTH);
    }

    /**
     * Increase the time by 1
     */
    public void tick() {
        tickCount++;
    }
}
