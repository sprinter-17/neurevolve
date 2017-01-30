package neurevolve.world;

public enum WorldInput {
    ELEVATION((w, p) -> w.getElevation(p)),
    TEMPERATURE((w, p) -> w.getTemperature(p)),
    RESOURCES((w, p) -> w.getResource(p)),
    RESOURCES_EAST((w, p) -> w.getResource(Direction.EAST.move(p))),
    RESOURCES_WEST((w, p) -> w.getResource(Direction.WEST.move(p))),
    RESOURCES_NORTH((w, p) -> w.getResource(Direction.NORTH.move(p))),
    RESOURCES_SOUTH((w, p) -> w.getResource(Direction.SOUTH.move(p))),;

    private interface ValueGetter {

        public int getValue(World world, Position position);
    }

    private final ValueGetter getter;

    private WorldInput(ValueGetter getter) {
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
}
