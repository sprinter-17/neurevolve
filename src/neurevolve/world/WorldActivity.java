package neurevolve.world;

import neurevolve.organism.Organism;

public enum WorldActivity {
    EAT("Eat", (w, o) -> w.feedOrganism(o)),
    DIVIDE("Divide", (w, o) -> w.splitOrganism(o)),
    MOVE("Move", (w, o) -> w.moveOrganism(o)),
    LEFT("Turn Left", (w, o) -> o.setDirection((o.getDirection() + 3) % 4)),
    RIGHT("Turn Right", (w, o) -> o.setDirection((o.getDirection() + 1) % 4));

    private interface ActivityPerformer {

        public void perform(World world, Organism organism);
    }

    private final String name;
    private final ActivityPerformer performer;

    private WorldActivity(String name, ActivityPerformer performer) {
        this.name = name;
        this.performer = performer;
    }

    public void perform(World world, Organism organism) {
        performer.perform(world, organism);
    }

    public static WorldActivity decode(int code) {
        final int length = values().length;
        return values()[((code % length) + length) % length];
    }

    public static void perform(int activity, World world, Organism organism) {
        decode(activity).perform(world, organism);
    }

    public static String print(int code) {
        return decode(code).name;
    }
}
