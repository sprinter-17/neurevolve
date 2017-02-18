package neurevolve.world;

import neurevolve.organism.Organism;
import static neurevolve.world.Angle.BACKWARD;
import static neurevolve.world.Angle.FORWARD;
import static neurevolve.world.Angle.LEFT;
import static neurevolve.world.Angle.RIGHT;

public enum WorldInput {
    AGE("Own Age", (w, o) -> o.getAge()),
    OWN_ENERGY("Own Energy", (w, o) -> o.getEnergy()),
    TEMPERATURE("Temperature", (w, o) -> w.getTemperature(w.getPosition(o))),
    LOOK_SLOPE_FORWARD("Slope Forward", (w, o) -> w.getSlope(o, FORWARD)),
    LOOK_SLOPE_LEFT("Slope Left", (w, o) -> w.getSlope(o, LEFT)),
    LOOK_SLOPE_RIGHT("Slope Right", (w, o) -> w.getSlope(o, RIGHT)),
    LOOK_SPACE_FORWARD("Space Forward", getSpace(FORWARD)),
    LOOK_SPACE_FAR_FORWARD("Space Far Forward", getSpace(FORWARD, FORWARD)),
    LOOK_SPACE_LEFT("Space Left", getSpace(LEFT)),
    LOOK_SPACE_RIGHT("Space Right", getSpace(RIGHT)),
    LOOK_ACID_HERE("Acid Here", getAcid()),
    LOOK_ACID_FORWARD("Acid Forward", getAcid(FORWARD)),
    LOOK_ACID_FAR_FORWARD("Acid Far Forward", getAcid(FORWARD, FORWARD)),
    LOOK_ACID_LEFT("Acid Left", getAcid(LEFT)),
    LOOK_ACID_RIGHT("Acid Right", getAcid(RIGHT)),
    LOOK_RADIATION_HERE("Radition Here", getRadiation()),
    LOOK_RADIATION_FORWARD("Radition Forward", getRadiation(FORWARD)),
    LOOK_RADIATION_FAR_FORWARD("Radition Far Forward", getRadiation(FORWARD, FORWARD)),
    LOOK_RADIATION_RIGHT("Radition Right", getRadiation(RIGHT)),
    LOOK_RADIATION_LEFT("Radition Left", getRadiation(LEFT)),
    LOOK_ORGANISM_BACKWARD("Colour Backward", getColourDifference(BACKWARD)),
    LOOK_ORGANISM_FORWARD("Colour Forward", getColourDifference(FORWARD)),
    LOOK_ORGANISM_LEFT("Colour Left", getColourDifference(LEFT)),
    LOOK_ORGANISM_RIGHT("Colour Right", getColourDifference(RIGHT)),
    LOOK_ORGANISM_FAR_FORWARD("Colour Far Forward", getColourDifference(FORWARD, FORWARD)),
    LOOK_ORGANISM_ENERGY_FORWARD("Organism Energy Forward", getEnergy(FORWARD)),
    LOOK_ORGANISM_ENERGY_FAR_FORWARD("Organism Energy Far Forward", getEnergy(FORWARD)),
    LOOK_RESOURCE_HERE("Resource Here", getResource()),
    LOOK_RESOURCE_FORWARD("Resource Forward", getResource(FORWARD)),
    LOOK_RESOURCE_LEFT("Resource Left", getResource(LEFT)),
    LOOK_RESOURCE_RIGHT("Resource Right", getResource(RIGHT)),
    LOOK_RESOURCE_FAR_FORWARD("Resource Far Forward", getResource(FORWARD, FORWARD)),;

    private interface ValueGetter {

        public int getValue(World world, Organism organism);
    }

    private final String name;
    private final ValueGetter getter;

    private WorldInput(String name, ValueGetter getter) {
        this.name = name;
        this.getter = getter;
    }

    public int getValue(World world, Organism organism) {
        return getter.getValue(world, organism);
    }

    public static WorldInput decode(int code) {
        final int count = values().length;
        return values()[Math.floorMod(code, count)];
    }

    public static int getValue(int input, World world, Organism organism) {
        return decode(input).getValue(world, organism);
    }

    public static String describe(int code) {
        return decode(code).name;
    }

    @FunctionalInterface
    private interface WorldValueGetter {

        int getValue(World world, Organism organism, int position);
    }

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

}
