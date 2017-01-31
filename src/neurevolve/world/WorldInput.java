package neurevolve.world;

import neurevolve.organism.Organism;
import static neurevolve.world.Frame.Direction.EAST;
import static neurevolve.world.Frame.Direction.NORTH;
import static neurevolve.world.Frame.Direction.SOUTH;
import static neurevolve.world.Frame.Direction.WEST;

public enum WorldInput {
    OWN_ENERGY("Energy", (w, p, o) -> o.getEnergy()),
    ELEVATION("Height", (w, p, o) -> w.getElevation(p)),
    TEMPERATURE("Temp", (w, p, o) -> w.getTemperature(p)),
    RESOURCES("Look Down", (w, p, o) -> w.getResource(p)),
    RESOURCES_EAST("Look East", (w, p, o) -> w.getResource(w.move(p, EAST))),
    RESOURCES_WEST("Look West", (w, p, o) -> w.getResource(w.move(p, WEST))),
    RESOURCES_NORTH("Look North", (w, p, o) -> w.getResource(w.move(p, NORTH))),
    RESOURCES_SOUTH("Look South", (w, p, o) -> w.getResource(w.move(p, SOUTH))),;

    private interface ValueGetter {

        public int getValue(World world, int position, Organism organism);
    }

    private final String name;
    private final ValueGetter getter;

    private WorldInput(String name, ValueGetter getter) {
        this.name = name;
        this.getter = getter;
    }

    public int getValue(World world, int position, Organism organism) {
        return getter.getValue(world, position, organism);
    }

    public static WorldInput decode(int code) {
        final int count = values().length;
        return values()[((code % count) + count) % count];
    }

    public static int getValue(int input, World world, int position, Organism organism) {
        return decode(input).getValue(world, position, organism);
    }

    public static String print(int code) {
        return decode(code).name;
    }
}
