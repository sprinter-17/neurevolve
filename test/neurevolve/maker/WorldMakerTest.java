package neurevolve.maker;

import java.util.stream.IntStream;
import neurevolve.TestConfiguration;
import neurevolve.world.Space;
import neurevolve.world.World;
import neurevolve.world.WorldConfiguration;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class WorldMakerTest {

    private final Space space = new Space(100, 100);
    private final WorldConfiguration config = new TestConfiguration();
    private WorldMaker maker;

    @Before
    public void setup() {
        maker = new WorldMaker(space, config);
    }

    @Before
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
    public void testSetResourcesEverywhere() {
        maker.add(maker.atStart(), maker.addResources(7), maker.everywhere());
        World world = maker.make();
        assertThat(allPositions().map(world::getResource).sum(), is(70000));
    }

    private IntStream allPositions() {
        return IntStream.range(0, space.size());
    }

}
