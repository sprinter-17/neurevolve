package neurevolve.maker;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;
import neurevolve.network.SigmoidFunction;
import neurevolve.world.Configuration;
import static neurevolve.world.Configuration.Value.YEAR_LENGTH;
import neurevolve.world.GroundElement;
import static neurevolve.world.GroundElement.*;
import neurevolve.world.Space;
import neurevolve.world.Time.Season;
import neurevolve.world.World;

/**
 * This class is used to construct a new world
 */
public class WorldMaker {

    private final Space space;
    private final Configuration config;
    private final List<Element> elements = new ArrayList<>();
    private final Random random = new Random();
    private final EnumSet<GroundElement> usedElements = EnumSet.noneOf(GroundElement.class);

    /**
     * An element within the world with a timing, type and shape
     */
    private class Element {

        private final Timing timing;
        private final Type type;
        private final Shape shape;

        /**
         * Construct a new element
         */
        public Element(Timing timing, Type type, Shape shape) {
            this.type = type;
            this.shape = shape;
            this.timing = timing;
        }

        /**
         * Apply the element to a world and configuration
         */
        public void apply(World world, Configuration config) {
            shape.forEachPosition((p, f) -> type.apply(world, p, f));
        }
    }

    /**
     * Construct a new {@code WorldMaker}
     *
     * @param space the space the world will be made within
     * @param config the configuration to adjust
     */
    public WorldMaker(Space space, Configuration config) {
        this.space = space;
        this.config = config;
    }

    /**
     * Create an acid element.
     *
     * @return a type used to place acid in the world
     */
    public Type acid() {
        usedElements.add(ACID);
        return (world, position, factor)
                -> world.addElementValue(position, ACID, 1);
    }

    /**
     * Create a radiation element.
     *
     * @param radiation the amount of radiation to place
     * @return a type used to place radiation in the world
     */
    public Type radiation(int radiation) {
        usedElements.add(RADIATION);
        return (world, position, factor)
                -> world.addElementValue(position, RADIATION, factor * radiation / 100);
    }

    /**
     * Create a wall element.
     *
     * @return a type used to place walls in the world
     */
    public Type wall() {
        usedElements.add(WALL);
        return (world, position, factor)
                -> world.addElementValue(position, WALL, 1);
    }

    /**
     * Create an elevation element.
     *
     * @param maxElevation the highest elevation to place
     * @return a type used to add to the elevation in the world
     */
    public Type elevation(int maxElevation) {
        usedElements.add(ELEVATION);
        return (world, position, factor)
                -> world.addElementValue(position, ELEVATION, factor * maxElevation / 100);
    }

    /**
     * Create a resource element.
     *
     * @param resources the amount of resources to add
     * @return a type used to add resources to the world
     */
    public Type addResources(int resources) {
        usedElements.add(GroundElement.RESOURCES);
        return (world, position, factor)
                -> world.addElementValue(position, RESOURCES, resources * factor / 100);
    }

    /**
     * Create a shape that places the element equally in every position.
     *
     * @return the shape
     */
    public Shape everywhere() {
        return action -> IntStream.range(0, space.size())
                .forEach(p -> action.accept(p, 100));
    }

    /**
     * Create a shape that places the element along the top and bottom edges of the space. If the
     * element can have a range of values then the highest value is at the edge of each band.
     *
     * @param depth the width of the shape
     * @return the shape
     */
    public Shape horizontalEdges(int depth) {
        return action -> IntStream.range(0, space.getWidth())
                .forEach(x -> IntStream.range(0, depth).forEach(y -> {
                    int factor = 100 - x * 100 / depth;
                    action.accept(space.position(x, y), factor);
                    action.accept(space.position(x, space.getHeight() - 1 - y), factor);
                }));
    }

    /**
     * Create a shape that places the element along the left and right edges of the space. If the
     * element can have a range of values then the highest value is at the edge of each band.
     *
     * @param depth the width of the shape
     * @return the shape
     */
    public Shape verticalEdges(int depth) {
        return action -> IntStream.range(0, space.getHeight())
                .forEach(y -> IntStream.range(0, depth).forEach(x -> {
                    int factor = 100 - x * 100 / depth;
                    action.accept(space.position(x, y), factor);
                    action.accept(space.position(space.getWidth() - 1 - x, y), factor);
                }));
    }

    /**
     * Create a shape that places the element in evenly spaced horizontal bands. If the element can
     * have a range of values then the highest value is at the centre of each band.
     *
     * @param count the number of bands to place
     * @param width the width of each band
     * @param gap the distance the bands reach from the left and right edges
     * @return the shape
     */
    public Shape horizontalDividers(int count, int width, int gap) {
        int scaledGap = scaleWidth(gap);
        return action -> IntStream.range(0, count)
                .map(i -> (i + 1) * space.getHeight() / (count + 1))
                .forEach(yc -> horizontalWall(yc, scaledGap, space.getWidth() - scaledGap, width, action));
    }

    /**
     * Create a single horizontal band
     */
    private void horizontalWall(int yc, int x1, int x2, int width, BiConsumer<Integer, Integer> action) {
        for (int x = x1; x < x2; x++) {
            for (int y = -width / 2; y < width / 2; y++) {
                int factor = 100 - Math.abs(y) * 200 / width;
                action.accept(space.position(x, yc + y), factor);
            }
        }
    }

    /**
     * Create a shape that places the element in evenly spaced vertical bands. If the element can
     * have a range of values then the highest value is at the centre of each band.
     *
     * @param count the number of bands to place
     * @param width the width of each band
     * @param gap the distance the bands reach from the top and bottom edges
     * @return the shape
     */
    public Shape verticalDividers(int count, int width, int gap) {
        int scaledGap = scaleHeight(gap);
        return action -> IntStream.range(0, count)
                .map(i -> (i + 1) * space.getWidth() / (count + 1))
                .forEach(x -> verticalWall(x, scaledGap, space.getHeight() - scaledGap, width, action));
    }

    /**
     * Create a single vertical band
     */
    private void verticalWall(int xc, int y1, int y2, int width, BiConsumer<Integer, Integer> action) {
        for (int y = y1; y < y2; y++) {
            for (int x = -width / 2; x < width / 2; x++) {
                int factor = 100 - Math.abs(x) * 200 / width;
                action.accept(space.position(xc + x, y), factor);
            }
        }
    }

    /**
     * Create a shape that places the element in randomly placed and randomly sized circles. If the
     * element can have a range of values then the highest value is at the centre of each circle.
     *
     * @param count the number of circles to place
     * @param radius the maximum radius of each circle
     * @return the shape
     */
    public Shape pools(int count, int radius) {
        int scaledRadius = Math.min(scaleHeight(radius), scaleWidth(radius));
        return action -> IntStream.range(0, count).forEach(i -> makePool(scaledRadius, action));
    }

    /**
     * Randomly place a single randomly sized circle
     */
    private void makePool(int maxRadius, BiConsumer<Integer, Integer> action) {
        int position = random.nextInt(space.size());
        int radius = random.nextInt(maxRadius) + 1;
        space.forAllPositionsInCircle(position, random.nextInt(radius),
                (p, d) -> action.accept(p, 100 - d * 100 / radius));
    }

    /**
     * Create a randomly generated maze of elements. The maze will be as large as possible for a
     * given cell size and wall width.
     *
     * @param cellWidth the height and width of each cell in the maze.
     * @param wallWidth the width of the walls of the maze.
     * @return the shape.
     */
    public Shape maze(int cellWidth, int wallWidth) {
        int cellSize = cellWidth + wallWidth;
        return action -> {
            int mazeWidth = (space.getWidth() - wallWidth) / cellSize;
            int mazeHeight = (space.getHeight() - wallWidth) / cellSize;
            int gapX = (space.getWidth() - mazeWidth * cellSize - wallWidth) / 2;
            int gapY = (space.getHeight() - mazeHeight * cellSize - wallWidth) / 2;
            Maze maze = new Maze(mazeWidth, mazeHeight);
            for (int mx = 0; mx <= mazeWidth; mx++) {
                for (int my = 0; my <= mazeHeight; my++) {
                    if (mx < mazeWidth && maze.hasWall(mx, my, Maze.Direction.SOUTH)) {
                        horizontalWall(gapY + my * cellSize + wallWidth / 2, gapX + mx * cellSize,
                                gapX + (mx + 1) * cellSize + wallWidth, wallWidth, action);
                    }
                    if (my < mazeHeight && maze.hasWall(mx, my, Maze.Direction.WEST)) {
                        verticalWall(gapX + mx * cellSize + wallWidth / 2, gapY + my * cellSize,
                                gapY + (my + 1) * cellSize + wallWidth, wallWidth, action);
                    }
                }
            }
        };
    }

    /**
     * Create a timing that places the element when the world is constructed.
     *
     * @return the timing
     */
    public Timing atStart() {
        return time -> time == 0;
    }

    /**
     * Create a timing that places the element at regular intervals.
     *
     * @param period the number of ticks between each placement of the element
     * @return the timing
     */
    public Timing withPeriod(int period) {
        return time -> time % period == 0;
    }

    /**
     * Create a timing that places the element in every tick during a season
     *
     * @param season the season to place the element in.
     * @return the timing
     */
    public Timing duringSeason(Season season) {
        return time -> Season.valueOf(time, config.getValue(YEAR_LENGTH)) == season;
    }

    @FunctionalInterface
    public interface Type {

        // radiation
        public void apply(World world, int position, int factor);
    }

    public interface Shape {

        public void forEachPosition(BiConsumer<Integer, Integer> action);
    }

    public interface Timing {
        // summer
        // winter
        // high population

        public boolean shouldMake(int time);
    }

    /**
     * Add a new element with a specific timing, type and shape.
     *
     * @param timing the element's timing
     * @param type the element's type
     * @param shape the element's shape
     */
    public void add(Timing timing, Type type, Shape shape) {
        elements.add(new Element(timing, type, shape));
    }

    /**
     * Make a new world with the given elements, space and configuration
     *
     * @return the constructed world
     */
    public World make() {
        World world = new World(new SigmoidFunction(100), space, config);
        usedElements.forEach(world::addUsedElement);
        process(world, 0);
        return world;
    }

    /**
     * Process all the elements that have been added.
     *
     * @param world the world to add the elements to
     */
    public void process(World world, int time) {
        elements.stream()
                .filter(e -> e.timing.shouldMake(time))
                .forEach(el -> el.apply(world, config));
    }

    private int scaleWidth(int value) {
        return value * space.getWidth() / 100;
    }

    private int scaleHeight(int value) {
        return value * space.getHeight() / 100;
    }
}
