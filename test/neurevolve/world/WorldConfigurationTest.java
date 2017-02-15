package neurevolve.world;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class WorldConfigurationTest {

    private WorldConfiguration config;

    @Before
    public void setup() {
        config = new WorldConfiguration();
    }

    @Test
    public void testDefaultActivityCost() {
        config.setDefaultActivityCost(7);
        assertThat(config.getActivityCost(WorldActivity.DIVIDE), is(7));
    }

    @Test
    public void testDefaultActivityFactor() {
        config.setDefaultActivityFactor(11);
        assertThat(config.getActivityFactor(WorldActivity.EAT_FORWARD), is(11));
    }

}
