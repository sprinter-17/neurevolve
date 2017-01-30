package neurevolve.world;

import java.util.function.UnaryOperator;

public enum Direction {
    NORTH(Position::north),
    EAST(Position::east),
    SOUTH(Position::south),
    WEST(Position::west);

    private final UnaryOperator<Position> move;

    private Direction(UnaryOperator<Position> move) {
        this.move = move;
    }

    public Position move(Position position) {
        return move.apply(position);
    }
}
