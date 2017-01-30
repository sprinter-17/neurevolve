package neurevolve.world;

public enum WorldActivity {
    EAT("Eat", (w, p) -> w.feedOrganism(p, 20)),
    DIVIDE("Divide", (w, p) -> w.splitOrganism(p)),
    MOVE_EAST("Move East", (w, p) -> w.moveOrganism(p, Direction.EAST)),
    MOVE_NORTH("Move North", (w, p) -> w.moveOrganism(p, Direction.NORTH)),
    MOVE_WEST("Move West", (w, p) -> w.moveOrganism(p, Direction.WEST)),
    MOVE_SOUTH("Move South", (w, p) -> w.moveOrganism(p, Direction.SOUTH)),;

    private interface ActivityPerformer {

        public void perform(World world, Position position);
    }

    private final String name;
    private final ActivityPerformer performer;

    private WorldActivity(String name, ActivityPerformer performer) {
        this.name = name;
        this.performer = performer;
    }

    public void perform(World world, Position position) {
        performer.perform(world, position);
    }

    public static void perform(int activity, World world, Position position) {
        if (activity >= 0)
            values()[activity % values().length].perform(world, position);
    }

    public static String print(int code) {
        return code < 0 ? "#" : values()[code % values().length].name;
    }
}
