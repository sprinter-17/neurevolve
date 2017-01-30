package neurevolve.world;

public class Position {

    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int latitude(int height) {
        return modulo(y, height) - height / 2;
    }

    public int toIndex(int width, int height) {
        return modulo(y, height) * width + modulo(x, width);
    }

    public Position wrap(int width, int height) {
        return new Position(modulo(x, width), modulo(y, height));
    }

    public static Position fromIndex(int index, int width, int height) {
        return new Position(modulo(index, width), modulo(index / width, height));
    }

    private static int modulo(int dividend, int divisor) {
        return ((dividend % divisor) + divisor) % divisor;
    }

    protected Position west() {
        return new Position(x - 1, y);
    }

    protected Position north() {
        return new Position(x, y + 1);
    }

    protected Position east() {
        return new Position(x + 1, y);
    }

    protected Position south() {
        return new Position(x, y - 1);
    }

    @Override
    public String toString() {
        return "Position{" + "x=" + x + ", y=" + y + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + this.x;
        hash = 41 * hash + this.y;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        final Position other = (Position) obj;
        return this.x == other.x && this.y == other.y;
    }

}
