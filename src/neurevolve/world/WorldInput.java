package neurevolve.world;

public enum WorldInput {
    ELEVATION((w, p) -> w.getElevation(p)),
    TEMPERATURE((w, p) -> w.getTemperature(p)),;

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
        if (input < 0 || input >= values().length)
            return 0;
        else
            return values()[input].getValue(world, position);
    }
}
