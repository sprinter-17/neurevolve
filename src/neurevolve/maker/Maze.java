package neurevolve.maker;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Maze {

    private final int width;
    private final int height;
    private final List<Path> paths = new ArrayList<>();

    private class Position {

        private final int x;
        private final int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Position east() {
            return new Position(x + 1, y);
        }

        public Position west() {
            return new Position(x - 1, y);
        }

        public Position north() {
            return new Position(x, y + 1);
        }

        public Position south() {
            return new Position(x, y - 1);
        }

        public boolean isInMaze() {
            return x >= 0 && x < width && y >= 0 && y < height;
        }

        public Stream<Position> neighbours() {
            return Stream.of(east(), west(), north(), south())
                    .filter(Position::isInMaze);
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

    private class Path {

        private final Position from;
        private final Position to;

        public Path(Position from, Position to) {
            this.from = from;
            this.to = to;
        }

        public boolean matches(Position position1, Position position2) {
            return from.equals(position1) && to.equals(position2)
                    || from.equals(position2) && to.equals(position1);
        }
    }

    public Maze(int width, int height) {
        this.width = width;
        this.height = height;
        Random random = new Random();
        Set<Position> visited = new HashSet<>();
        Deque<Position> stack = new ArrayDeque<>();
        stack.push(new Position(random.nextInt(width), random.nextInt(height)));
        while (!stack.isEmpty()) {
            Position current = stack.peek();
            visited.add(current);
            List<Position> neighbours = current.neighbours()
                    .filter(p -> !visited.contains(p))
                    .collect(Collectors.toList());
            if (neighbours.isEmpty()) {
                stack.pop();
            } else {
                Position next = neighbours.get(random.nextInt(neighbours.size()));
                paths.add(new Path(current, next));
                stack.push(next);
            }
        }
    }

    public boolean hasWallWest(int x, int y) {
        Position position = new Position(x, y);
        return paths.stream().noneMatch(p -> p.matches(position, position.west()));
    }

    public boolean hasWallNorth(int x, int y) {
        Position position = new Position(x, y);
        return paths.stream().noneMatch(p -> p.matches(position, position.north()));
    }

    public boolean hasWallEast(int x, int y) {
        Position position = new Position(x, y);
        return paths.stream().noneMatch(p -> p.matches(position, position.east()));
    }

    public boolean hasWallSouth(int x, int y) {
        Position position = new Position(x, y);
        return paths.stream().noneMatch(p -> p.matches(position, position.south()));
    }

}
