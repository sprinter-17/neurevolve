package neurevolve.world;

public enum WorldActivity {
    EAT((w, p) -> w.feedOrganism(p, 20)),
    DIVIDE((w, p) -> w.splitOrganism(p)),
    MOVE_EAST((w, p) -> w.moveOrganism(p, Direction.EAST)),
    MOVE_NORTH((w, p) -> w.moveOrganism(p, Direction.NORTH)),
    MOVE_WEST((w, p) -> w.moveOrganism(p, Direction.WEST)),
    MOVE_SOUTH((w, p) -> w.moveOrganism(p, Direction.SOUTH)),;

    private interface ActivityPerformer {

        public void perform(World world, Position position);
    }

    private final ActivityPerformer performer;

    private WorldActivity(ActivityPerformer performer) {
        this.performer = performer;
    }

    public void perform(World world, Position position) {
        performer.perform(world, position);
    }

    public static void perform(int activity, World world, Position position) {
        if (activity >= 0 && activity < values().length)
            values()[activity].perform(world, position);
    }
}
