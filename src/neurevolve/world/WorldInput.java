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
    LOOK_WALL_FORWARD("Wall Forward", getWall(FORWARD)),
    LOOK_WALL_FAR_FORWARD("Wall Far Forward", getWall(FORWARD, FORWARD)),
    LOOK_WALL_LEFT("Wall Left", getWall(LEFT)),
    LOOK_WALL_RIGHT("Wall Right", getWall(RIGHT)),
    LOOK_ACIDE_FORWARD("Acid Forward", getAcid(FORWARD)),
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

    private static ValueGetter getEnergy(Angle... angles) {
        return (world, organism) -> {
            int position = world.getPosition(organism, angles);
            return world.getOrganismEnergy(position);
        };
    }

    private static ValueGetter getResource(Angle... angles) {
        return (world, organism) -> {
            int position = world.getPosition(organism, angles);
            return world.getResource(position);
        };
    }

    private static ValueGetter getColourDifference(Angle... angles) {
        return (world, organism) -> {
            int position = world.getPosition(organism, angles);
            return world.getColourDifference(organism, position);
        };
    }

    private static ValueGetter getWall(Angle... angles) {
        return (world, organism) -> {
            int position = world.getPosition(organism, angles);
            return world.hasWall(position) ? 100 : 0;
        };
    }

    private static ValueGetter getAcid(Angle... angles) {
        return (world, organism) -> {
            int position = world.getPosition(organism, angles);
            return world.isAcidic(position) ? 100 : 0;
        };
    }

}
