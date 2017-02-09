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

        private final Type type;
        private final Shape shape;
        private final Timing timing;

        public Element(Timing timing, Type type, Shape shape) {
            this.type = type;
            this.shape = shape;
            this.timing = timing;
        }

        public void apply(World world, WorldConfiguration config) {
            shape.forEachPosition((p, f) -> type.apply(world, p, f));
        }
    }

    public Type acid() {
        return (world, position, factor) -> world.setAcidic(position, true);
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

    public Shape pools(int count, int radius) {
        return action -> IntStream.range(0, count).forEach(i -> makePool(radius, action));
    }

    private void makePool(int maxRadius, BiConsumer<Integer, Integer> action) {
        int position = random.nextInt(space.size());
        int radius = random.nextInt(maxRadius) + 1;
        space.forAllPositionsInCircle(position, random.nextInt(radius),
                (p, d) -> action.accept(p, 100 - d * 100 / radius));
    }

    public Timing atStart() {
        return new Timing() {

        };
    }

    public WorldMaker(Space space, WorldConfiguration config) {
        this.space = space;
        this.config = config;
    }

    @FunctionalInterface
    public interface Type {

        // resource growth
        // resource set
        // elevation
        // radiation
        public void apply(World world, int position, int factor);
    }

    public interface Shape {

        // all
        // small pools
        // medium pools
        // big pools
        // two sections
        // three sections
        // four sections
        // maze
        public void forEachPosition(BiConsumer<Integer, Integer> action);
    }

    public interface Timing {
        // summer
        // winter
        // permanent
        // high population
    }

    public void add(Timing timing, Type type, Shape shape) {
        elements.add(new Element(timing, type, shape));
    }

    public World make() {
        World world = new World(new SigmoidFunction(1000), space, config);
        elements.forEach(el -> el.apply(world, config));
        return world;
    }

}
