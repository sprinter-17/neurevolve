package neurevolve.world;

/**
 * Represents angles from a direction. This enumeration is used to specify the angle from an
 * organisms current direction.
 */
public enum Angle {
    FORWARD,
    LEFT,
    BACKWARD,
    RIGHT;

    /**
     * Calculate the direction that is at this angle from a given direction
     *
     * @param direction the source direction
     * @return the resulting direction
     */
    public int add(int direction) {
        return (direction + ordinal()) % 4;
    }
}
