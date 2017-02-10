package neurevolve.maker;

public class PrintSampleMaze {

    public static void main(String[] args) {
        Maze maze = new Maze(20, 6);
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 20; x++) {
                System.out.print("+" + (maze.hasWallSouth(x, y) ? "---" : "   "));
            }
            System.out.println("+");
            for (int x = 0; x < 20; x++) {
                System.out.print((maze.hasWallWest(x, y) ? "|" : " ") + "   ");
            }
            System.out.println("|");
        }
        for (int x = 0; x < 20; x++) {
            System.out.print("+---");
        }
        System.out.println("+");
    }
}
