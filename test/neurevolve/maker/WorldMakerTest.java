package neurevolve.maker;

import java.util.stream.IntStream;
import neurevolve.TestConfiguration;
import neurevolve.maker.WorldMaker.Shape;
import neurevolve.maker.WorldMaker.Type;
import neurevolve.world.Configuration;
import neurevolve.world.Configuration.Value;
import neurevolve.world.GroundElement;
import static neurevolve.world.GroundElement.*;
import neurevolve.world.Space;
import neurevolve.world.Time.Season;
import neurevolve.world.World;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class WorldMakerTest {

    private final Space space = new Space(100, 100);
    private final Configuration config = new TestConfiguration();
    private WorldMaker maker;

    @Before
    public void setup() {
        maker = new WorldMaker(space, config);
    }

    @Test
    public void testEmptyWorld() {
        World world = maker.make();
        assertThat(elements(world, space, RESOURCES).sum(), is(0));
        assertThat(elements(world, space, ELEVATION).sum(), is(0));
        assertThat(elements(world, space, ACID).sum(), is(0));
    }

    @Test
    public void testAcidEverywhere() {
        maker.add(maker.atStart(), maker.acid(), maker.everywhere());
        assertTrue(elements(maker.make(), space, ACID).allMatch(n -> n > 0));
    }

    @Test
    public void testRadiationAtVerticalEdges() {
        maker.add(maker.atStart(), maker.radiation(3), maker.verticalEdges(6));
        World world = maker.make();
        assertThat(world.getElementValue(space.position(0, 30), RADIATION), is(3));
        assertThat(world.getElementValue(space.position(1, 30), RADIATION), is(2));
        assertThat(world.getElementValue(space.position(2, 30), RADIATION), is(2));
        assertThat(world.getElementValue(space.position(3, 30), RADIATION), is(1));
        assertThat(world.getElementValue(space.position(4, 30), RADIATION), is(1));
        assertThat(world.getElementValue(space.position(5, 30), RADIATION), is(0));
    }

    @Test
    public void testWallsAtHorizontalEdges() {
        maker.add(maker.atStart(), maker.wall(), maker.horizontalEdges(5));
        World world = maker.make();
        assertThat(elements(world, space, WALL).sum(), is(1000));
        assertThat(world.getElementValue(space.position(0, 50), WALL), is(0));
        assertThat(world.getElementValue(space.position(50, 0), WALL), is(1));
    }

    @Test
    public void testWallsAtVerticalEdges() {
        maker.add(maker.atStart(), maker.wall(), maker.verticalEdges(5));
        World world = maker.make();
        assertThat(elements(world, space, WALL).sum(), is(1000));
        assertThat(world.getElementValue(space.position(0, 50), WALL), is(1));
        assertThat(world.getElementValue(space.position(50, 0), WALL), is(0));
    }

    @Test
    public void testDividers() {
        assertThat(wallCount(make(maker.wall(), maker.horizontalDividers(1, 30, 20))), is(30 * 60));
        assertThat(wallCount(make(maker.wall(), maker.horizontalDividers(3, 10, 10))), is(30 * 80));
    }

    @Test
    public void testSetResourcesEverywhere() {
        maker.add(maker.atStart(), maker.addResources(7), maker.everywhere());
        assertThat(elements(maker.make(), space, RESOURCES).sum(), is(70000));
    }

    @Test
    public void testMaze() {
        Space mazeSpace = new Space(800, 500);
        maker = new WorldMaker(mazeSpace, config);
        maker.add(maker.atStart(), maker.elevation(200), maker.maze(25, 8));
        World world = maker.make();
        assertTrue(elements(world, mazeSpace, ELEVATION).filter(e -> e > 0).count() > 80000);
    }

    @Test
    public void testRegularMaker() {
        maker.add(maker.withPeriod(2), maker.addResources(10), maker.everywhere());
        World world = maker.make();
        assertThat(resourceCount(world), is(100000));
        world.tick();
        assertThat(resourceCount(world), is(100000));
        world.tick();
        assertThat(resourceCount(world), is(200000));
        world.tick();
        assertThat(resourceCount(world), is(200000));
        world.tick();
        assertThat(resourceCount(world), is(300000));
    }

    @Test
    public void testSeasonalElements() {
        config.setValue(Value.YEAR_LENGTH, 8);
        config.setValue(Value.TEMP_VARIATION, 0);
        maker.add(maker.duringSeason(Season.SUMMER), maker.addResources(1), maker.everywhere());
        World world = maker.make();
        assertThat(resourceCount(world), is(0));
        world.tick();
        assertThat(resourceCount(world), is(0));
        world.tick();
        assertThat(resourceCount(world), is(0));
        world.tick();
        assertThat(resourceCount(world), is(10000));
        world.tick();
        assertThat(resourceCount(world), is(20000));
        world.tick();
        assertThat(resourceCount(world), is(20000));
        world.tick();
        assertThat(resourceCount(world), is(20000));
        world.tick();
        assertThat(resourceCount(world), is(20000));
        world.tick();
        assertThat(resourceCount(world), is(20000));
        world.tick();
        assertThat(resourceCount(world), is(20000));
        world.tick();
        assertThat(resourceCount(world), is(20000));
        world.tick();
        assertThat(resourceCount(world), is(30000));
    }

    private World make(Type type, Shape shape) {
        maker = new WorldMaker(space, config);
        maker.add(maker.atStart(), type, shape);
        return maker.make();
    }

    private int wallCount(World world) {
        return elements(world, space, WALL).sum();
    }

    private int resourceCount(World world) {
        return elements(world, space, RESOURCES).sum();
    }

    private IntStream elements(World world, Space space, GroundElement element) {
        return IntStream.range(0, space.size()).map(p -> world.getElementValue(p, element));
    }
}
