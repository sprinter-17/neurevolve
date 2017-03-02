package neurevolve.maker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generates a random maze of a given height and width
 */
public class Maze {

    private final int width;
    private final int height;
    private final List<Path> paths = new ArrayList<>();
    private final Random random = new Random();
    private final Set<Position> visited = new HashSet<>();
    private final Deque<Position> stack = new LinkedList<>();

    /**
     * A direction from one maze cell to an adjacent one
     */
    public enum Direction {
        NORTH(p -> new Position(p.x, p.y + 1)),
        EAST(p -> new Position(p.x + 1, p.y)),
        SOUTH(p -> new Position(p.x, p.y - 1)),
        WEST(p -> new Position(p.x - 1, p.y));

        private final UnaryOperator<Position> move;

        Direction(UnaryOperator<Position> move) {
            this.move = move;
        }

        private Position move(Position position) {
            return move.apply(position);
        }
    }

    /**
     * A cell position within the maze
     */
    private static class Position {

        private final int x;
        private final int y;

        private Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Get all neighbour positions in the maze
         */
        private Stream<Position> neighbours(Maze maze) {
            return Arrays.stream(Direction.values())
                    .map(dir -> dir.move(this))
                    .filter(p -> p.isInMaze(maze));
        }

        /**
         * @return true, if the position is in the given maze
         */
        private boolean isInMaze(Maze maze) {
            return x >= 0 && x < maze.width && y >= 0 && y < maze.height;
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 47 * hash + this.x;
            hash = 47 * hash + this.y;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            else if (obj == null || getClass() != obj.getClass())
                return false;
            final Position other = (Position) obj;
            return this.x == other.x && this.y == other.y;
        }

    }

    /**
     * An open path from a cell to an adjacent cell
     */
    private class Path {

        private final Position from;
        private final Position to;

        public Path(Position from, Position to) {
            this.from = from;
            this.to = to;
        }

        /**
         * @return true if the two given positions are the two positions in the path
         */
        public boolean matches(Position position1, Position position2) {
            return from.equals(position1) && to.equals(position2)
                    || from.equals(position2) && to.equals(position1);
        }
    }

    /**
     * Construct a maze. The maze allows a single path between any two cells. Once constructed, the
     * structure of the maze can be determined by calling {@link #hasWall}
     *
     * @param width the number of cells horizontally
     * @param height the number of cells vertically
     */
    public Maze(int width, int height) {
        this.width = width;
        this.height = height;
        generateMaze();
    }

    /**
     * Generate the maze
     */
    private void generateMaze() {
        stack.push(new Position(random.nextInt(width), random.nextInt(height)));
        while (!stack.isEmpty()) {
            extendOrBacktrack();
        }
    }

    /**
     * If possible extend the maze from the current position. If not then backtrack and try again.
     */
    private void extendOrBacktrack() {
        List<Position> neighbours = getNeighbours();
        if (neighbours.isEmpty())
            stack.pop();
        else
            addNeighbour(neighbours.get(random.nextInt(neighbours.size())));
    }

    /**
     * Get a random neighbour for the position (if any)
     */
    private List<Position> getNeighbours() {
        return stack.peek().neighbours(this)
                .filter(p -> !visited.contains(p))
                .collect(Collectors.toList());
    }

    /**
     * Add a path to a neighbour and then push it onto the stack
     */
    private void addNeighbour(Position neighbour) {
        paths.add(new Path(stack.peek(), neighbour));
        stack.push(neighbour);
        visited.add(neighbour);
    }

    /**
     * Check if a cell has a wall in a given direction
     *
     * @param x the x position of the cell
     * @param y the y position of the cell
     * @param direction the direction to check
     * @return true if there is a wall in the given direction from the cell
     */
    public boolean hasWall(int x, int y, Direction direction) {
        Position position = new Position(x, y);
        return paths.stream()
                .noneMatch(p -> p.matches(position, direction.move(position)));
    }

}
