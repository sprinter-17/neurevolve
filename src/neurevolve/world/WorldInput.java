package neurevolve.world;

import neurevolve.organism.Organism;

public enum WorldInput {
    OWN_ENERGY("Energy", (w, o) -> o.getEnergy()),
    ELEVATION("Height", (w, o) -> w.getElevation(o.getPosition())),
    TEMPERATURE("Temp", (w, o) -> w.getTemperature(o.getPosition())),
    RESOURCES("Look Down", (w, o) -> w.getResource(o.getPosition())),
    RESOURCES_EAST("Look Forward", (w, o) -> w.getResource(o.getPosition(), o.getDirection())),
    RESOURCES_WEST("Look Left", (w, o) -> w.getResource(o.getPosition(), (o.getDirection() + 3) % 4)),
    RESOURCES_NORTH("Look Right", (w, o) -> w.getResource(o.getPosition(), (o.getDirection() + 1) % 4));

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
}
