package neurevolve.world;

import neurevolve.organism.Organism;
import static neurevolve.world.Angle.FORWARD;
import static neurevolve.world.Angle.LEFT;
import static neurevolve.world.Angle.RIGHT;

public enum WorldInput {
    AGE("Age", (w, o) -> o.getAge()),
    OWN_ENERGY("Eng", (w, o) -> o.getEnergy()),
    TEMPERATURE("Tmp", (w, o) -> w.getTemperature(w.getPosition(o))),
    LOOK_SLOPE_FORWARD("Lk SF", (w, o) -> w.getSlope(o, FORWARD)),
    LOOK_SLOPE_LEFT("Lk SL", (w, o) -> w.getSlope(o, LEFT)),
    LOOK_SLOPE_RIGHT("Lk SR", (w, o) -> w.getSlope(o, RIGHT)),
    LOOK_ORGANISM_FORWARD("Lk OF", getOrganism(FORWARD)),
    LOOK_ORGANISM_LEFT("Lk OL", getOrganism(LEFT)),
    LOOK_ORGANISM_RIGHT("Lk OR", getOrganism(RIGHT)),
    LOOK_ORGANISM_FAR_FORWARD("Lk OFF", getOrganism(FORWARD, FORWARD)),
    LOOK_RESOURCE_HERE("Lk RH", getResource()),
    LOOK_RESOURCE_FORWARD("Lk RF", getResource(FORWARD)),
    LOOK_RESOURCE_LEFT("Lk RL", getResource(LEFT)),
    LOOK_RESOURCE_RIGHT("Lk RR", getResource(RIGHT)),
    LOOK_RESOURCE_FAR_FORWARD("Lk RFF", getResource(FORWARD, FORWARD)),;

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
        return values()[((code % count) + count) % count];
    }

    public static int getValue(int input, World world, Organism organism) {
        return decode(input).getValue(world, organism);
    }

    public static String print(int code) {
        return decode(code).name;
    }

    private static ValueGetter getOrganism(Angle... angles) {
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

    public static int convertToColour(WorldConfiguration config, int resources, Organism organism, int elevation) {
        return resourceColour(config, resources)
                | populationColour(config, organism)
                | elevationColour(config, elevation);
    }

    private static int resourceColour(WorldConfiguration config, int resource) {
        return (resource * 255 / config.getMaxResources()) << 8;
    }

    private static int populationColour(WorldConfiguration config, Organism organism) {
        return organism == null ? 0 : 200 << 16;
    }

    private static int elevationColour(WorldConfiguration config, int elevation) {
        return elevation * 255 / config.getMaxElevation();
    }
}
