package neurevolve.world;

import neurevolve.world.Configuration.Value;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class TimeTest {

    private Configuration config;
    private Time time;

    @Before
    public void setup() {
        config = new Configuration();
        time = new Time(config);
    }

    @Test
    public void testStartTime() {
        assertThat(time.getTime(), is(0));
    }

    @Test
    public void testTick() {
        time.tick();
        assertThat(time.getTime(), is(1));
        time.tick();
        assertThat(time.getTime(), is(2));
    }

    @Test
    public void testSeasons() {
        config.setValue(Value.YEAR_LENGTH, 8);
        config.setValue(Value.TEMP_VARIATION, 4);
        assertThat(time.getSeasonName(), is("Winter"));
        assertThat(time.getSeasonalTemp(), is(-4));
        time.tick();
        assertThat(time.getSeasonName(), is("Spring"));
        assertThat(time.getSeasonalTemp(), is(-2));
        time.tick();
        assertThat(time.getSeasonName(), is("Spring"));
        assertThat(time.getSeasonalTemp(), is(0));
        time.tick();
        assertThat(time.getSeasonName(), is("Summer"));
        assertThat(time.getSeasonalTemp(), is(+2));
        time.tick();
        assertThat(time.getSeasonName(), is("Summer"));
        assertThat(time.getSeasonalTemp(), is(+4));
        time.tick();
        assertThat(time.getSeasonName(), is("Autumn"));
        assertThat(time.getSeasonalTemp(), is(+2));
        time.tick();
        assertThat(time.getSeasonName(), is("Autumn"));
        assertThat(time.getSeasonalTemp(), is(0));
        time.tick();
        assertThat(time.getSeasonName(), is("Winter"));
        assertThat(time.getSeasonalTemp(), is(-2));
        time.tick();
        assertThat(time.getSeasonName(), is("Winter"));
        assertThat(time.getSeasonalTemp(), is(-4));
    }
}
