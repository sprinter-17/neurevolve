package neurevolve.world;

import java.util.Optional;
import neurevolve.TestConfiguration;
import neurevolve.organism.Organism;
import neurevolve.world.Configuration.Value;
import static neurevolve.world.GroundElement.*;
import static neurevolve.world.Space.EAST;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class WorldActivityTest {

    private Configuration config;
    private Space frame;
    private World world;
    private int position;
    private Organism organism;

    @Before
    public void setup() {
        config = new TestConfiguration();
        frame = new Space(10, 10);
        world = new World(n -> n, frame, config);
        position = frame.position(5, 7);
        organism = new Organism(world, 100);
        world.addOrganism(organism, position, EAST);
    }

    @Test
    public void testMove() {
        assertFalse(world.hasOrganism(frame.move(position, EAST)));
        WorldActivity.MOVE.perform(world, organism);
        assertTrue(world.hasOrganism(frame.move(position, EAST)));
    }

    @Test
    public void testEat() {
        config.setValue(Value.CONSUMPTION_RATE, 20);
        world.addElementValue(position, RESOURCES, 100);
        WorldActivity.EAT_HERE.perform(world, organism);
        assertThat(organism.getEnergy(), is(120));
        assertThat(world.getElementValue(position, RESOURCES), is(80));
    }

    @Test
    public void testEatToMaxEnergy() {
        config.setValue(Value.CONSUMPTION_RATE, 20);
        config.setValue(Value.MAX_ENERGY, 110);
        world.addElementValue(position, RESOURCES, 100);
        WorldActivity.EAT_HERE.perform(world, organism);
        assertThat(organism.getEnergy(), is(110));
        assertThat(world.getElementValue(position, RESOURCES), is(90));
    }

    @Test
    public void testDivide() {
        WorldActivity.DIVIDE.perform(world, organism);
        assertThat(organism.getEnergy(), is(50));
        assertThat(world.getPopulationSize(), is(2));
    }

    @Test
    public void testWithName() {
        assertThat(WorldActivity.withName("Fred"), is(Optional.empty()));
        assertThat(WorldActivity.withName("split"), is(Optional.of(WorldActivity.DIVIDE)));
        assertThat(WorldActivity.withName("SPLIT"), is(Optional.of(WorldActivity.DIVIDE)));
    }
}
