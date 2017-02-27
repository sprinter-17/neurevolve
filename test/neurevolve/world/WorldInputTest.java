package neurevolve.world;

import java.util.EnumSet;
import neurevolve.TestConfiguration;
import neurevolve.organism.Organism;
import static neurevolve.world.Angle.FORWARD;
import static neurevolve.world.Angle.LEFT;
import static neurevolve.world.Angle.RIGHT;
import static neurevolve.world.Space.EAST;
import static neurevolve.world.Space.NORTH;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class WorldInputTest {

    private WorldConfiguration config;
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
    public void testOwnAge() {
        assertThat(input("Own Age"), is(0));
        world.tick();
        assertThat(input("Own Age"), is(1));
    }

    @Test
    public void testOwnEnergy() {
        assertThat(input("Own Energy"), is(100));
    }

    @Test
    public void testOwnTemperature() {
        assertThat(input("Temperature Here"), is(0));
        config.setTemperatureRange(50, 50);
        assertThat(input("Temperature Here"), is(50));
    }

    @Test
    public void testSlope() {
        input.setUsedElements(EnumSet.of(GroundElement.ELEVATION));
        assertThat(input("Look Slope Forward"), is(0));
        world.addElevation(frame.move(position, 0), 17);
        assertThat(input("Look Slope Forward"), is(17));
        assertThat(input("Look Slope Left"), is(0));
    }

    @Test
    public void testWall() {
        input.setUsedElements(EnumSet.of(GroundElement.WALL));
        assertThat(input("Look Wall Far Forward"), is(0));
        world.setWall(world.getPosition(organism, FORWARD, FORWARD), true);
        assertThat(input("Look Wall Far Forward"), is(WorldInput.MAX_VALUE));
    }

    @Test
    public void testAcid() {
        input.setUsedElements(EnumSet.of(GroundElement.ACID));
        assertThat(input("Look Acid Forward Left"), is(0));
        world.setAcidic(world.getPosition(organism, FORWARD, LEFT), true);
        assertThat(input("Look Acid Forward Left"), is(WorldInput.MAX_VALUE));
    }

    @Test
    public void testOtherColour() {
        assertThat(input("Look Other Colour Forward Right"), is(-100));
        Organism other = new Organism(world, 100, 15);
        world.addOrganism(other, world.getPosition(organism, FORWARD, RIGHT), NORTH);
        assertThat(input("Look Other Colour Forward Right"), is(4));
    }

    private int input(String name) {
        return input.getValue(organism, input.getCode(name).getAsInt());
    }
}
