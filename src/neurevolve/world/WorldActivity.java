package neurevolve.world;

import neurevolve.organism.Organism;

public enum WorldActivity {
    EAT("Eat", (w, p, o) -> w.feedOrganism(p, 20)),
    DIVIDE("Divide", (w, p, o) -> w.splitOrganism(p)),
    MOVE_EAST("Move East", (w, p, o) -> w.moveOrganism(p, Direction.EAST)),
    MOVE_NORTH("Move North", (w, p, o) -> w.moveOrganism(p, Direction.NORTH)),
    MOVE_WEST("Move West", (w, p, o) -> w.moveOrganism(p, Direction.WEST)),
    MOVE_SOUTH("Move South", (w, p, o) -> w.moveOrganism(p, Direction.SOUTH)),;

    private interface ActivityPerformer {

        public void perform(World world, int position, Organism organism);
    }

    private final String name;
    private final ActivityPerformer performer;

    private WorldActivity(String name, ActivityPerformer performer) {
        this.name = name;
        this.performer = performer;
    }

    public void perform(World world, int position, Organism organism) {
        performer.perform(world, position, organism);
    }

    public static WorldActivity decode(int code) {
        final int length = values().length;
        return values()[((code % length) + length) % length];
    }

    public static void perform(int activity, World world, int position, Organism organism) {
        decode(activity).perform(world, position, organism);
    }

    public static String print(int code) {
        return decode(code).name;
    }
}
