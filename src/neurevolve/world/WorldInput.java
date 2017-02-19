package neurevolve.world;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import neurevolve.organism.Organism;
import static neurevolve.world.Angle.FORWARD;
import static neurevolve.world.Angle.LEFT;
import static neurevolve.world.Angle.RIGHT;

public class WorldInput {

    public static final int MAX_VALUE = 100;

    @FunctionalInterface
    private interface ValueGetter {

        int getValue(Organism organism);
    }

    private class WorldValueGetter {

        private final String name;
        private final ValueGetter valueGetter;

        public WorldValueGetter(String name, ValueGetter valueGetter) {
            this.name = name;
            this.valueGetter = valueGetter;
        }

        public String getName() {
            return name;
        }

        public int getValue(Organism organism) {
            return valueGetter.getValue(organism);
        }
    }

    private final World world;
    private final List<WorldValueGetter> valueGetters = new ArrayList<>();

    private enum VisionField {
        LOOK_HERE("Here"),
        LOOK_FORWARD("Forward", FORWARD),
        LOOK_FAR_FORWARD("Far Forward", FORWARD, FORWARD),
        LOOK_LEFT("Left", LEFT),
        LOOK_FORWARD_LEFT("Forward Left", FORWARD, LEFT),
        LOOK_FAR_LEFT("Far Left", LEFT, LEFT),
        LOOK_RIGHT("Right", RIGHT),
        LOOK_FORWARD_RIGHT("Forward Right", FORWARD, RIGHT),
        LOOK_FAR_RIGHT("Far Right", RIGHT, RIGHT);

        private final String name;
        private final Angle[] angles;

        VisionField(String name, Angle... angles) {
            this.name = name;
            this.angles = angles;
        }
    }

    public WorldInput(World world) {
        this.world = world;
        addInput("Own Age", Organism::getAge);
        addInput("Own Energy", Organism::getEnergy);
        addInput("Temperature Here", o -> world.getTemperature(world.getPosition(o)));
        addInput("Look Slope Forward", o -> world.getSlope(o, FORWARD));
        addInput("Look Slope Left", o -> world.getSlope(o, LEFT));
        addInput("Look Slope Right", o -> world.getSlope(o, RIGHT));
        addVisionInput("Other Energy", (o, p) -> world.getOrganismEnergy(p));
        addVisionInput("Other Colour", world::getColourDifference);
        addVisionElementInput(GroundElement.WALL);
        addVisionElementInput(GroundElement.ACID);
        addVisionElementInput(GroundElement.RESOURCES);
        addVisionElementInput(GroundElement.BODY);
        addVisionElementInput(GroundElement.RADIATION);
    }

    private void addInput(String name, ValueGetter valueGetter) {
        valueGetters.add(new WorldValueGetter(name, valueGetter));
    }

    private void addVisionInput(String name, BiFunction<Organism, Integer, Integer> getter) {
        for (VisionField field : VisionField.values()) {
            String fieldName = "Look " + name + " " + field.name;
            addInput(fieldName, o -> getter.apply(o, world.getPosition(o, field.angles)));
        }
    }

    private void addVisionElementInput(GroundElement element) {
        addVisionInput(element.getName(), (o, p) -> getValue(p, element));
    }

    private int getValue(int position, GroundElement element) {
        return world.getElementValue(position, element) * MAX_VALUE / element.getMaximum();
    }

    public String getName(int code) {
        return decode(code).name;
    }

    public int getValue(Organism organism, int code) {
        return decode(code).valueGetter.getValue(organism);
    }

    public int getCode(String name) {
        return IntStream.range(0, valueGetters.size())
                .filter(i -> valueGetters.get(i).name.equals(name))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No input with name " + name));
    }

    private WorldValueGetter decode(int code) {
        return valueGetters.get(Math.floorMod(code, valueGetters.size()));
    }

    /*
    private static ValueGetter getEnergy(Angle... angles) {
        return getWorldValueGetter((w, o, p) -> w.getOrganismEnergy(p), angles);
    }
    private static ValueGetter getResource(Angle... angles) {
        return getWorldValueGetter((w, o, p) -> w.getResource(p), angles);
    }

    private static ValueGetter getColourDifference(Angle... angles) {
        return getWorldValueGetter((w, o, p) -> w.getColourDifference(o, p));
    }

    private static ValueGetter getSpace(Angle... angles) {
        return getWorldValueGetter((w, o, p) -> w.isEmpty(p) ? 100 : 0, angles);
    }

    private static ValueGetter getAcid(Angle... angles) {
        return getWorldValueGetter((w, o, p) -> w.isAcidic(p) ? 100 : 0, angles);
    }

    private static ValueGetter getRadiation(Angle... angles) {
        return getWorldValueGetter((w, o, p) -> w.getRadiation(p), angles);
    }

    private static ValueGetter getWorldValueGetter(WorldValueGetter getter, Angle... angles) {
        return (world, organism) -> getter.getValue(world, organism, world.getPosition(organism, angles));
    }
     */
}
