package neurevolve;

import neurevolve.network.SigmoidFunction;
import neurevolve.organism.Organism;
import neurevolve.organism.RecipePrinter;
import neurevolve.ui.MainWindow;
import neurevolve.world.Frame;
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

        Frame frame = new Frame(1000, 500);

        World world = new World(new SigmoidFunction(1000), frame, config);
        MainWindow window = new MainWindow(world, frame, config);
        window.show();

        for (int t = 0; t < 50000; t++) {
            world.tick();
            Organism mostComplex = world.getLargestOrganism();
            System.out.print(world.getTime() + ":" + world.getPopulationSize()
                    + " (" + String.format("%.2f", world.getAverageComplexity()) + ")"
                    + " FM: " + Runtime.getRuntime().freeMemory());
            if (mostComplex != null)
                System.out.print(" ^ " + mostComplex.complexity() + ":" + mostComplex.toString(PRINTER));
            System.out.println();
        }
    }
}
