package neurevolve.world;

import neurevolve.organism.Organism;
import static neurevolve.world.Angle.FORWARD;
import static neurevolve.world.Angle.LEFT;
import static neurevolve.world.Angle.RIGHT;

public enum WorldActivity {
    EAT_HERE("Eat H", (w, o) -> w.feedOrganism(o)),
    EAT_FORWARD("Eat F", (w, o) -> w.feedOrganism(o, FORWARD)),
    DIVIDE("Split", (w, o) -> w.splitOrganism(o)),
    MOVE("Move", (w, o) -> w.moveOrganism(o)),
    KILL("Kill", (w, o) -> w.killOrganism(o, FORWARD)),
    TURN_LEFT("Turn L", (w, o) -> w.turn(o, LEFT)),
    TURN_RIGHT("Turn R", (w, o) -> w.turn(o, RIGHT));

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
