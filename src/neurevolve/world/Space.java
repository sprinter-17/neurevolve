package neurevolve.world;

import java.util.function.BiConsumer;

/**
 * A <code>Space</code> represents a limited area within which Cartesian coordinates operate but in
 * which opposite edges are considered to be joined. It has a fixed width and height. A position
 * within the frame is represented as an integer. There are methods to move a position in each of
 * the four directions with the value wrapping at the edges of the frame.
 *
 * @author simon
 */
public class Space {

    public static final int EAST = 0;
    public static final int NORTH = 1;
    public static final int WEST = 2;
    public static final int SOUTH = 3;

    private final int width;
    private final int height;

    /**
     * Construct a <code>Frame</code>.
     *
     * @param width the horizontal size of the frame
     * @param height the vertical size of the frame
     * @throws IllegalArgumentException if <tt>width &le; 0 || height &le; 0</tt>
     */
    public Space(int width, int height) {
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("Zero size frame");
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
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
     * Calculate a position from cartesian coordinates with origin at top left
     *
     * @param x the horizontal distance of the position from the left edge
     * @param y the vertical distance of the position from the top edge
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
    public int move(int position, int direction) {
        checkPosition(position);
        int x = x(position);
        int y = y(position);
        switch (direction) {
            case EAST:
                return position((x + 1) % width, y);
            case NORTH:
                return position(x, (y - 1 + height) % height);
            case WEST:
                return position((x - 1 + width) % width, y);
            case SOUTH:
                return position(x, (y + 1) % height);
            default:
                throw new IllegalArgumentException("Illegal direction for move");
        }
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

    public void forAllPositionsInCircle(int centre, int radius, BiConsumer<Integer, Integer> action) {
        int xc = x(centre);
        int yc = y(centre);
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                int r = (int) Math.sqrt(x * x + y * y);
                if (r <= radius)
                    action.accept(position(((xc + x) + width) % width, ((yc + y) + height) % height), r);
            }
        }
    }

    /**
     * Calculate the horizontal distance of a position from the left edge
     */
    private int x(int position) {
        return position % width;
    }

    /**
     * Calculate the vertical distance of a position from the top edge
     */
    private int y(int position) {
        return position / width;
    }
}
