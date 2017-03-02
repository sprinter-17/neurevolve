package neurevolve.maker;

import java.util.stream.IntStream;
import neurevolve.TestConfiguration;
import neurevolve.maker.WorldMaker.Shape;
import neurevolve.maker.WorldMaker.Type;
import neurevolve.world.Space;
import neurevolve.world.Time.Season;
import neurevolve.world.World;
import neurevolve.world.Configuration;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
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
        assertTrue(allPositions().map(world::getResource).allMatch(r -> r == 0));
        assertTrue(allPositions().map(world::getElevation).allMatch(r -> r == 0));
        assertTrue(allPositions().noneMatch(world::isAcidic));
    }

    @Test
    public void testAcidEverywhere() {
        maker.add(maker.atStart(), maker.acid(), maker.everywhere());
        World world = maker.make();
        assertTrue(allPositions().allMatch(world::isAcidic));
    }

    @Test
    public void testRadiationAtVerticalEdges() {
        maker.add(maker.atStart(), maker.radiation(3), maker.verticalEdges(6));
        World world = maker.make();
        assertThat(world.getRadiation(space.position(0, 30)), is(3));
        assertThat(world.getRadiation(space.position(1, 30)), is(2));
        assertThat(world.getRadiation(space.position(2, 30)), is(2));
        assertThat(world.getRadiation(space.position(3, 30)), is(1));
        assertThat(world.getRadiation(space.position(4, 30)), is(1));
        assertThat(world.getRadiation(space.position(5, 30)), is(0));
    }

    @Test
    public void testWallsAtHorizontalEdges() {
        maker.add(maker.atStart(), maker.wall(), maker.horizontalEdges(5));
        World world = maker.make();
        assertThat(allPositions().filter(world::hasWall).count(), is(1000L));
        assertFalse(world.hasWall(space.position(0, 50)));
        assertTrue(world.hasWall(space.position(50, 0)));
    }

    @Test
    public void testWallsAtVerticalEdges() {
        maker.add(maker.atStart(), maker.wall(), maker.verticalEdges(5));
        World world = maker.make();
        assertThat(allPositions().filter(world::hasWall).count(), is(1000L));
        assertTrue(world.hasWall(space.position(0, 50)));
        assertFalse(world.hasWall(space.position(50, 0)));
    }

    @Test
    public void testDividers() {
        assertThat(wallCount(make(maker.wall(), maker.horizontalDividers(1, 30, 20))), is(30 * 60));
        assertThat(wallCount(make(maker.wall(), maker.horizontalDividers(3, 10, 10))), is(30 * 80));
    }

    @Test
    public void testSetResourcesEverywhere() {
        maker.add(maker.atStart(), maker.addResources(7), maker.everywhere());
        World world = maker.make();
        assertThat(allPositions().map(world::getResource).sum(), is(70000));
    }

    @Test
    public void testMaze() {
        Space mazeSpace = new Space(800, 500);
        maker = new WorldMaker(mazeSpace, config);
        maker.add(maker.atStart(), maker.elevation(200), maker.maze(25, 8));
        World world = maker.make();
        assertTrue(IntStream.range(0, mazeSpace.size())
                .filter(p -> world.getElevation(p) > 0).count() > 80000);
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
        config.setYear(8, 0);
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

    private IntStream allPositions() {
        return IntStream.range(0, space.size());
    }

    private World make(Type type, Shape shape) {
        maker = new WorldMaker(space, config);
        maker.add(maker.atStart(), type, shape);
        return maker.make();
    }

    private int wallCount(World world) {
        return (int) allPositions().filter(world::hasWall).count();
    }

    private int resourceCount(World world) {
        return allPositions().map(p -> world.getResource(p)).sum();
    }

}
