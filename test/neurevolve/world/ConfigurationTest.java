package neurevolve.world;

import neurevolve.world.Configuration.Value;
import static neurevolve.world.Configuration.Value.HALF_LIFE;
import static neurevolve.world.GroundElement.ACID;
import static neurevolve.world.GroundElement.WALL;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class ConfigurationTest {

    private Configuration config;

    @Before
    public void setup() {
        config = new Configuration();
    }

    @Test
    public void testDefaultActivityCost() {
        config.setValue(Value.ACTIVITY_COST, 7);
        assertThat(config.getActivityCost(WorldActivity.DIVIDE), is(7));
    }

    @Test
    public void testDefaultActivityFactor() {
        config.setValue(Value.ACTIVITY_FACTOR, 11);
        assertThat(config.getActivityFactor(WorldActivity.EAT_FORWARD), is(11));
    }

    @Test
    public void testHalfLife() {
        config.setHalfLife(ACID, 8);
        assertThat(config.getHalfLife(ACID), is(8));
        assertThat(config.getHalfLife(WALL), is(HALF_LIFE.getDefault()));
    }

}
