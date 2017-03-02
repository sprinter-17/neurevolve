package neurevolve.world;

import neurevolve.TestConfiguration;
import neurevolve.TestEnvironment;
import neurevolve.organism.Organism;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class WorldStatisticsTest {

    private World world;
    private WorldStatistics stats;

    @Before
    public void setup() {
        Configuration config = new TestConfiguration();
        Time time = new Time(config);
        stats = new WorldStatistics(time);
    }

    @Test
    public void testStartStats() {
        assertThat(stats.getAverageAge(), is(0f));
        assertThat(stats.getAverageComplexity(), is(0f));
        assertThat(stats.getAverageDescendents(), is(0f));
        assertThat(stats.getAverageEnergy(), is(0f));
        assertThat(stats.getAverageSize(), is(0f));
        assertThat(stats.getPopulation(), is(0f));
    }

    @Test
    public void testAddOrganism() {
        stats.add(new Organism(new TestEnvironment(), 50));
        stats.add(new Organism(new TestEnvironment(), 100));
        assertThat(stats.getAverageEnergy(), is(75f));
    }

}
