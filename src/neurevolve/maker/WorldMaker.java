package neurevolve.maker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;
import neurevolve.network.SigmoidFunction;
import neurevolve.world.Space;
import neurevolve.world.World;
import neurevolve.world.WorldConfiguration;

public class WorldMaker {

    private final Space space;
    private final WorldConfiguration config;
    private final List<Element> elements = new ArrayList<>();
    private final Random random = new Random();

    private class Element {

        private final Timing timing;
        private final Type type;
        private final Shape shape;

        public Element(Timing timing, Type type, Shape shape) {
            this.type = type;
            this.shape = shape;
            this.timing = timing;
        }

        public void apply(World world, WorldConfiguration config) {
            shape.forEachPosition((p, f) -> type.apply(world, p, f));
        }
    }

    public WorldMaker(Space space, WorldConfiguration config) {
        this.space = space;
        this.config = config;
    }

    public Type acid() {
        return (world, position, factor) -> world.setAcidic(position, true);
    }

    public Type radiation(int radiation) {
        return (world, position, factor) -> world.addRadition(position, factor * radiation / 100);
    }

    public Type wall() {
        return (world, position, factor) -> world.setWall(position, true);
    }

    public Type elevation(int maxElevation) {
        return (world, position, factor) -> world.addElevation(position, factor * maxElevation / 100);
    }

    public Type addResources(int resources) {
        return (world, position, factor) -> world.addResources(position, resources * factor / 100);
    }

    public Shape everywhere() {
        return action -> IntStream.range(0, space.size())
                .forEach(p -> action.accept(p, 100));
    }

    public Shape horizontalEdges(int depth) {
        return action -> IntStream.range(0, space.getWidth())
                .forEach(x -> IntStream.range(0, depth).forEach(y -> {
                    int factor = 100 - x * 100 / depth;
                    action.accept(space.position(x, y), factor);
                    action.accept(space.position(x, space.getHeight() - 1 - y), factor);
                }));
    }

    public Shape verticalEdges(int depth) {
        return action -> IntStream.range(0, space.getHeight())
                .forEach(y -> IntStream.range(0, depth).forEach(x -> {
                    int factor = 100 - x * 100 / depth;
                    action.accept(space.position(x, y), factor);
                    action.accept(space.position(space.getWidth() - 1 - x, y), factor);
                }));
    }

    public Shape horizontalDividers(int count, int width, int gap) {
        return action -> IntStream.range(0, count)
                .map(i -> (i + 1) * space.getHeight() / (count + 1))
                .forEach(yc -> horizontalWall(yc, gap, space.getWidth() - gap, width, action));
    }

    private void horizontalWall(int yc, int x1, int x2, int width, BiConsumer<Integer, Integer> action) {
        for (int x = x1; x < x2; x++) {
            for (int y = -width / 2; y < width / 2; y++) {
                int factor = 100 - Math.abs(y) * 200 / width;
                action.accept(space.position(x, yc + y), factor);
            }
        }
    }

    public Shape verticalDividers(int count, int width, int gap) {
        return action -> IntStream.range(0, count)
                .map(i -> (i + 1) * space.getWidth() / (count + 1))
                .forEach(x -> verticalWall(x, gap, space.getHeight() - gap, width, action));
    }

    private void verticalWall(int xc, int y1, int y2, int width, BiConsumer<Integer, Integer> action) {
        for (int y = y1; y < y2; y++) {
            for (int x = -width / 2; x < width / 2; x++) {
                int factor = 100 - Math.abs(x) * 200 / width;
                action.accept(space.position(xc + x, y), factor);
            }
        }
    }

    public Shape pools(int count, int radius) {
        return action -> IntStream.range(0, count).forEach(i -> makePool(radius, action));
    }

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
                    if (mx < mazeWidth && maze.hasWallSouth(mx, my)) {
                        horizontalWall(gapY + my * cellSize + wallWidth / 2, gapX + mx * cellSize,
                                gapX + (mx + 1) * cellSize + wallWidth, wallWidth, action);
                    }
                    if (my < mazeHeight && maze.hasWallWest(mx, my)) {
                        verticalWall(gapX + mx * cellSize + wallWidth / 2, gapY + my * cellSize,
                                gapY + (my + 1) * cellSize + wallWidth, wallWidth, action);
                    }
                }
            }
        };
    }

    private void makePool(int maxRadius, BiConsumer<Integer, Integer> action) {
        int position = random.nextInt(space.size());
        int radius = random.nextInt(maxRadius) + 1;
        space.forAllPositionsInCircle(position, random.nextInt(radius),
                (p, d) -> action.accept(p, 100 - d * 100 / radius));
    }

    public Timing atStart() {
        return time -> time == 0;
    }

    public Timing withPeriod(int period) {
        return time -> time % period == 0;
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
        // permanent
        // high population

        public boolean shouldMake(int time);
    }

    public void add(Timing timing, Type type, Shape shape) {
        elements.add(new Element(timing, type, shape));
    }

    public World make() {
        World world = new World(new SigmoidFunction(1000), space, config);
        process(world);
        world.addTickListener(() -> process(world));
        return world;
    }

    private void process(World world) {
        elements.stream()
                .filter(e -> e.timing.shouldMake(world.getTime()))
                .forEach(el -> el.apply(world, config));
    }

}
