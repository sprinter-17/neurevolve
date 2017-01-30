package neurevolve.world;

public enum WorldInput {
    ELEVATION("Height", (w, p) -> w.getElevation(p)),
    TEMPERATURE("Temp", (w, p) -> w.getTemperature(p)),
    RESOURCES("Look Down", (w, p) -> w.getResource(p)),
    RESOURCES_EAST("Look East", (w, p) -> w.getResource(Direction.EAST.move(p))),
    RESOURCES_WEST("Look West", (w, p) -> w.getResource(Direction.WEST.move(p))),
    RESOURCES_NORTH("Look North", (w, p) -> w.getResource(Direction.NORTH.move(p))),
    RESOURCES_SOUTH("Look South", (w, p) -> w.getResource(Direction.SOUTH.move(p))),;

    private interface ValueGetter {

        public int getValue(World world, Position position);
    }

    private final String name;
    private final ValueGetter getter;

    private WorldInput(String name, ValueGetter getter) {
        this.name = name;
        this.getter = getter;
    }

    public int getValue(World world, Position position) {
        return getter.getValue(world, position);
    }

    public static int getValue(int input, World world, Position position) {
        if (input < 0)
            return 0;
        else
            return values()[input % values().length].getValue(world, position);
    }

    public static String print(int code) {
        return code < 0 ? "#" : values()[code % values().length].name;
    }
}
