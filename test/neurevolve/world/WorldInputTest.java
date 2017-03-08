package neurevolve.world;

import neurevolve.TestConfiguration;
import neurevolve.organism.Organism;
import static neurevolve.world.Angle.*;
import neurevolve.world.Configuration.Value;
import static neurevolve.world.GroundElement.*;
import static neurevolve.world.Space.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class WorldInputTest {

    private Configuration config;
    private Space frame;
    private World world;
    private int position;
    private Organism organism;
    private WorldInput input;

    @Before
    public void setup() {
        config = new TestConfiguration();
        frame = new Space(10, 10);
        world = new World(n -> n, frame, config);
        position = frame.position(5, 6);
        organism = new Organism(world, 100);
        world.addOrganism(organism, position, EAST);
        input = new WorldInput(world);
    }

    @Test
    public void testUsesElements() {
        assertTrue(input.usesElement(BODY));
        assertTrue(input.getCode("Look Body Here").isPresent());
        assertTrue(input.usesElement(RESOURCES));
        assertTrue(input.getCode("Look Resources Here").isPresent());
        assertFalse(input.getCode("Look Acid Here").isPresent());
        assertFalse(input.usesElement(ACID));
        input.addUsedElement(ACID);
        assertTrue(input.usesElement(ACID));
        assertTrue(input.getCode("Look Acid Here").isPresent());
        assertFalse(input.usesElement(ELEVATION));
        input.addUsedElement(ELEVATION);
        assertTrue(input.usesElement(ACID));
        assertTrue(input.usesElement(ELEVATION));
    }

    @Test
    public void testOwnAge() {
        assertThat(input("Own Age"), is(0));
        WorldTicker ticker = new WorldTicker(world, config);
        ticker.tick();
        assertThat(input("Own Age"), is(1));
    }

    @Test
    public void testOwnEnergy() {
        assertThat(input("Own Energy"), is(100));
    }

    @Test
    public void testOwnTemperature() {
        assertThat(input("Temperature Here"), is(0));
        config.setValue(Value.MIN_TEMP, 50);
        config.setValue(Value.MAX_TEMP, 50);
        assertThat(input("Temperature Here"), is(50));
    }

    @Test
    public void testSlope() {
        input.addUsedElement(ELEVATION);
        assertThat(input("Look Slope Forward"), is(0));
        world.addElementValue(frame.move(position, EAST), ELEVATION, 17);
        assertThat(input("Look Slope Forward"), is(17));
        assertThat(input("Look Slope Left"), is(0));
    }

    @Test
    public void testWall() {
        input.addUsedElement(WALL);
        assertThat(input("Look Wall Far Forward"), is(-WorldInput.MAX_VALUE));
        world.addElementValue(world.getPosition(organism, FORWARD, FORWARD), WALL, 1);
        assertThat(input("Look Wall Far Forward"), is(WorldInput.MAX_VALUE));
    }

    @Test
    public void testAcid() {
        input.addUsedElement(ACID);
        assertThat(input("Look Acid Forward Left"), is(-1));
        world.addElementValue(world.getPosition(organism, FORWARD, LEFT), ACID, 1);
        assertThat(input("Look Acid Forward Left"), is(WorldInput.MAX_VALUE - 1));
    }

    @Test
    public void testOtherColour() {
        assertThat(input("Look Other Colour Forward Right"), is(-WorldInput.MAX_VALUE));
        Organism other = new Organism(world, 100, 15);
        world.addOrganism(other, world.getPosition(organism, FORWARD, RIGHT), NORTH);
        assertThat(input("Look Other Colour Forward Right"), is(3));
    }

    private int input(String name) {
        return input.getValue(organism, input.getCode(name).getAsInt());
    }
}
