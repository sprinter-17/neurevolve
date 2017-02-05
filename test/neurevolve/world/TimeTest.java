package neurevolve.world;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class TimeTest {

    private WorldConfiguration config;
    private Time time;

    @Before
    public void setup() {
        config = new WorldConfiguration();
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
        config.setYear(8, -8);
        assertThat(time.getSeasonName(), is("Spring"));
        assertThat(time.getSeasonalTemp(), is(-8));
        time.tick();
        assertThat(time.getSeasonName(), is("Spring"));
        assertThat(time.getSeasonalTemp(), is(-6));
        time.tick();
        assertThat(time.getSeasonName(), is("Summer"));
        assertThat(time.getSeasonalTemp(), is(-4));
        time.tick();
        assertThat(time.getSeasonName(), is("Summer"));
        assertThat(time.getSeasonalTemp(), is(-2));
        time.tick();
        assertThat(time.getSeasonName(), is("Autumn"));
        assertThat(time.getSeasonalTemp(), is(0));
        time.tick();
        assertThat(time.getSeasonName(), is("Autumn"));
        assertThat(time.getSeasonalTemp(), is(-2));
        time.tick();
        assertThat(time.getSeasonName(), is("Winter"));
        assertThat(time.getSeasonalTemp(), is(-4));
        time.tick();
        assertThat(time.getSeasonName(), is("Winter"));
        assertThat(time.getSeasonalTemp(), is(-6));
        time.tick();
        assertThat(time.getSeasonName(), is("Spring"));
        assertThat(time.getSeasonalTemp(), is(-8));
    }
}
