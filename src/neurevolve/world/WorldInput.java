package neurevolve.world;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import neurevolve.organism.Organism;
import static neurevolve.world.Angle.FORWARD;
import static neurevolve.world.Angle.LEFT;
import static neurevolve.world.Angle.RIGHT;

public class WorldInput {

    public static final int MAX_VALUE = 100;
    private final World world;
    private final List<WorldValueGetter> valueGetters = new ArrayList<>();

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
        addVisionInput("Other Energy", (o, p) -> world.hasOrganism(p) ? world.getOrganismEnergy(p) : -MAX_VALUE);
        addVisionInput("Other Colour", world::getColourDifference);
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

    public void setUsedElements(EnumSet<GroundElement> elements) {
        elements.forEach(this::addVisionElementInput);
    }

    private void addVisionElementInput(GroundElement element) {
        addVisionInput(element == GroundElement.ELEVATION ? "Slope" : element.getName(), (o, p) -> getValue(o, p, element));
    }

    private int getValue(Organism organism, int position, GroundElement element) {
        switch (element) {
            case WALL:
            case BODY:
                return world.getElementValue(position, element) > 0 ? MAX_VALUE : -MAX_VALUE;
            case ELEVATION:
                return world.getSlope(organism, position);
            case RADIATION:
            case ACID:
            case RESOURCES:
                return MAX_VALUE * world.getElementValue(position, element) / element.getMaximum() - 1;
            default:
                throw new AssertionError(element.name());
        }
    }

    public String getName(int code) {
        return decode(code).name;
    }

    public int getValue(Organism organism, int code) {
        return decode(code).valueGetter.getValue(organism);
    }

    public int getCodeCount() {
        return valueGetters.size();
    }

    public OptionalInt getCode(String name) {
        return IntStream.range(0, valueGetters.size())
                .filter(i -> valueGetters.get(i).name.equalsIgnoreCase(name.replace("_", " ")))
                .findAny();
    }

    private WorldValueGetter decode(int code) {
        return valueGetters.get(Math.floorMod(code, valueGetters.size()));
    }
}
