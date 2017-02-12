package neurevolve.maker;

import static neurevolve.maker.Maze.Direction.SOUTH;
import static neurevolve.maker.Maze.Direction.WEST;

public class PrintSampleMaze {

    public static void main(String[] args) {
        Maze maze = new Maze(20, 6);
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 20; x++) {
                System.out.print("+" + (maze.hasWall(x, y, SOUTH) ? "---" : "   "));
            }
            System.out.println("+");
            for (int x = 0; x < 20; x++) {
                System.out.print((maze.hasWall(x, y, WEST) ? "|" : " ") + "   ");
            }
            System.out.println("|");
        }
        for (int x = 0; x < 20; x++) {
            System.out.print("+---");
        }
        System.out.println("+");
    }
}
