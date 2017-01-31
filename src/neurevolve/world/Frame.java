package neurevolve.world;

import java.util.function.IntBinaryOperator;

/**
 * A <code>Frame</code> represents a limited area within which cartesian coordinates operate but in
 * which opposite edges are considered to be joined. It has a fixed width and height. A position
 * within the frame is represented as an integer. There are methods to move a position in each of
 * the four directions with the value wrapping at the edges of the frame.
 *
 * @author simon
 */
public class Frame {

    private final int width;
    private final int height;

    /**
     * <code>Direction</code> enumerates the four directions in which a position can move.
     */
    public enum Direction {
        NORTH((x, w) -> x, (y, h) -> (y + 1) % h),
        EAST((x, w) -> (x + 1) % w, (y, h) -> y),
        SOUTH((x, w) -> x, (y, h) -> (y - 1 + h) % h),
        WEST((x, w) -> (x - 1 + w) % w, (y, h) -> y),;

        private final IntBinaryOperator xMove;
        private final IntBinaryOperator yMove;

        private Direction(IntBinaryOperator xMove, IntBinaryOperator yMove) {
            this.xMove = xMove;
            this.yMove = yMove;
        }

        private int move(Frame frame, int position) {
            int x = xMove.applyAsInt(frame.x(position), frame.width);
            int y = yMove.applyAsInt(frame.y(position), frame.height);
            return frame.position(x, y);
        }
    }

    /**
     * Construct a <code>Frame</code>.
     *
     * @param width the horizontal size of the frame
     * @param height the vertical size of the frame
     * @throws IllegalArgumentException if <tt>width &le; 0 || height &le; 0</tt>
     */
    public Frame(int width, int height) {
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("Zero size frame");
        this.width = width;
        this.height = height;
    }

    /**
     * Get the total number of positions in the frame
     *
     * @return <tt>width * height</tt>
     */
    public int size() {
        return width * height;
    }

    /**
     * Calculate a position from cartesian coordinates
     *
     * @param x the horizontal distance of the position from the left edge
     * @param y the vertical distance of the position from the bottom edge
     * @return the position of the coordinates
     * @throws IllegalArgumentException if the coordinates are not within the frame
     */
    public int position(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            throw new IllegalArgumentException("Coordinates out of frame");
        return y * width + x;
    }

    /**
     * Calculate a new position in a given direction. Will wrap around the edges of the frame.
     *
     * @param position the starting position
     * @param direction the direction to move
     * @return the position in the given direction from the starting position
     * @throws IllegalArgumentException if the position is illegal
     */
    public int move(int position, Direction direction) {
        checkPosition(position);
        return direction.move(this, position);
    }

    /**
     * Throws an IllegalArgumentException if the given position is illegal
     */
    private void checkPosition(int position) {
        if (position < 0 || position >= size())
            throw new IllegalArgumentException("Illegal position");
    }

    /**
     * Scale a value between two limits by the vertical distance of a position from the middle of
     * the frame.
     *
     * @param position the position to use to calculate the value
     * @param min the minimum value of the range
     * @param max the maximum value of the range
     * @return the value within the range represented by the vertical distance of the position
     * @throws IllegalArgumentException if the position is illegal
     */
    public int scaleByLatitude(int position, int min, int max) {
        checkPosition(position);
        return max - (2 * (max - min) * latitude(position) / height);
    }

    /**
     * Calculate the vertical distance of a position from the midpoint of the frame
     */
    private int latitude(int position) {
        return Math.abs(y(position) - height / 2);
    }

    /**
     * Calculate the horizontal distance of a position from the left edge
     */
    private int x(int position) {
        return position % width;
    }

    /**
     * Calculate the vertical distance of a position from the bottom edge
     */
    private int y(int position) {
        return position / width;
    }
}
