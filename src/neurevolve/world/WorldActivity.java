package neurevolve.world;

import java.util.Arrays;
import java.util.Optional;
import neurevolve.organism.Organism;
import static neurevolve.world.Angle.FORWARD;
import static neurevolve.world.Angle.LEFT;
import static neurevolve.world.Angle.RIGHT;

public enum WorldActivity {
    EAT_HERE("Eat Here", (w, o) -> w.feedOrganism(o)),
    EAT_FORWARD("Eat Forward", (w, o) -> w.feedOrganism(o, FORWARD)),
    DIVIDE("Split", (w, o) -> w.splitOrganism(o)),
    MOVE("Move", (w, o) -> w.moveOrganism(o)),
    ATTACK("Attack", (w, o) -> w.attackOrganism(o, FORWARD)),
    TURN_LEFT("Turn Left", (w, o) -> w.turnOrganism(o, LEFT)),
    TURN_RIGHT("Turn Right", (w, o) -> w.turnOrganism(o, RIGHT));

    private interface ActivityPerformer {

        public boolean perform(World world, Organism organism);
    }

    private final String name;
    private final ActivityPerformer performer;

    private WorldActivity(String name, ActivityPerformer performer) {
        this.name = name;
        this.performer = performer;
    }

    public byte code() {
        return (byte) ordinal();
    }

    public boolean perform(World world, Organism organism) {
        return performer.perform(world, organism);
    }

    public static Optional<WorldActivity> withName(String name) {
        return Arrays.stream(values())
                .filter(a -> a.name.equalsIgnoreCase(name))
                .findAny();
    }

    public static WorldActivity decode(int code) {
        final int length = values().length;
        return values()[((code % length) + length) % length];
    }

    public static String describe(int code) {
        return decode(code).name;
    }
}
