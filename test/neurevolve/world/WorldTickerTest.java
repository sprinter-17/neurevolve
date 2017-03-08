package neurevolve.world;

import java.util.stream.IntStream;
import neurevolve.TestConfiguration;
import neurevolve.organism.Organism;
import static neurevolve.world.GroundElement.ACID;
import static neurevolve.world.GroundElement.RADIATION;
import static neurevolve.world.GroundElement.RESOURCES;
import static neurevolve.world.Space.EAST;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class WorldTickerTest {

    private Configuration config;
    private Space space;
    private World world;
    private WorldTicker ticker;

    @Before
    public void setup() {
        config = new TestConfiguration();
        space = new Space(10, 10);
        world = new World(n -> n, space, config);
        ticker = new WorldTicker(world, config);
    }

    @Test
    public void testTime() {
        assertThat(ticker.getTime(), is(0));
        ticker.tick();
        assertThat(ticker.getTime(), is(1));
    }

    @Test
    public void testTemperatureEffectByTimeOfYear() {
        int position = space.position(7, 4);
        config.setValue(Configuration.Value.YEAR_LENGTH, 8);
        config.setValue(Configuration.Value.TEMP_VARIATION, 50);
        assertThat(ticker.getTemperature(position), is(-50));
        ticker.tick();
        assertThat(ticker.getTemperature(position), is(-25));
        ticker.tick();
        assertThat(ticker.getTemperature(position), is(0));
        ticker.tick();
        assertThat(ticker.getTemperature(position), is(+25));
        ticker.tick();
        assertThat(ticker.getTemperature(position), is(+50));
        ticker.tick();
        assertThat(ticker.getTemperature(position), is(+25));
        ticker.tick();
        assertThat(ticker.getTemperature(position), is(0));
        ticker.tick();
        assertThat(ticker.getTemperature(position), is(-25));
        ticker.tick();
        assertThat(ticker.getTemperature(position), is(-50));
    }

    @Test
    public void testNegativeTemperaturesConsumeEnergyFromOrganisms() {
        config.setValue(Configuration.Value.MIN_TEMP, -30);
        config.setValue(Configuration.Value.MAX_TEMP, -30);
        Organism organism = new Organism(world, 50);
        world.addOrganism(organism, space.position(4, 7), EAST);
        ticker.tick();
        assertThat(organism.getEnergy(), is(20));
        ticker.tick();
        assertThat(organism.getEnergy(), is(0));
    }

    @Test
    public void testRemoveDeadOrganisms() {
        int position = space.position(4, 7);
        Organism organism = new Organism(world, 0);
        world.addOrganism(organism, position, EAST);
        ticker.tick();
        assertFalse(world.hasOrganism(position));
    }

    @Test
    public void testResourcesGrowBasedOnTemperature() {
        config.setValue(Configuration.Value.MIN_TEMP, 0);
        config.setValue(Configuration.Value.MAX_TEMP, 200);
        ticker.tick();
        assertThat(world.getElementValue(space.position(0, 0), RESOURCES), is(0));
        assertThat(world.getElementValue(space.position(7, 3), RESOURCES), is(1));
        assertThat(world.getElementValue(space.position(4, 5), RESOURCES), is(2));
    }

    @Test
    public void testAcidHalfLife() {
        int position = space.position(4, 7);
        world.addElementValue(position, ACID, 1);
        ticker.tick();
        assertThat(world.getElementValue(position, ACID), is(1));
        config.setHalfLife(ACID, 1);
        ticker.tick();
        assertThat(world.getElementValue(position, ACID), is(0));
    }

    @Test
    public void testRadiationHalfLife() {
        IntStream.range(0, space.size()).forEach(p -> world.addElementValue(p, RADIATION, 3));
        config.setHalfLife(RADIATION, 10);
        assertThat(totalRadiation(), is(300));
        for (int i = 0; i < 10; i++) {
            ticker.tick();
        }
        assertTrue(totalRadiation() < 230);
        assertTrue(totalRadiation() > 170);
    }

    private int totalRadiation() {
        return IntStream.range(0, space.size()).map(p -> world.getElementValue(p, RADIATION)).sum();
    }
}
