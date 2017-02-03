package neurevolve;

import neurevolve.network.SigmoidFunction;
import neurevolve.organism.Organism;
import neurevolve.organism.RecipePrinter;
import neurevolve.ui.MainWindow;
import neurevolve.world.Space;
import neurevolve.world.World;
import neurevolve.world.WorldActivity;
import neurevolve.world.WorldConfiguration;
import neurevolve.world.WorldInput;

public class Neurevolve {

    private final static RecipePrinter PRINTER = new RecipePrinter() {
        @Override
        public String getInput(int code) {
            return WorldInput.print(code);
        }

        @Override
        public String getActivity(int code) {
            return WorldActivity.print(code);
        }
    };

    public static void main(String[] args) {

        WorldConfiguration config = new WorldConfiguration();
        config.setTemperatureRange(50, 200);
        config.setYear(200, -100);
        config.setMutationRate(20);
        config.setConsumptionRate(20);

        Space frame = new Space(1000, 500);

        World world = new World(new SigmoidFunction(1000), frame, config);
        world.addHill(frame.position(100, 400), 40, 5, 50);
        world.addHill(frame.position(200, 150), 60, 2, 40);
        world.addHill(frame.position(400, 250), 150, 1, 0);
        world.addHill(frame.position(700, 200), 60, 4, 10);
        world.addHill(frame.position(850, 120), 90, 2, 40);
        world.addHill(frame.position(900, 400), 70, 3, 40);
        MainWindow window = new MainWindow(world, frame, config);
        window.show();

        for (int t = 0; t < 50000; t++) {
            world.tick();
            Organism mostComplex = world.getMostComplexOrganism();
            System.out.print(world.getTime() + ":" + world.getPopulationSize()
                    + " (" + String.format("%.2f", world.getAverageComplexity()) + ")"
                    + " FM: " + Runtime.getRuntime().freeMemory());
            if (mostComplex != null)
                System.out.print(" ^ " + mostComplex.complexity() + ":" + mostComplex.toString(PRINTER));
            System.out.println();
        }
    }
}
