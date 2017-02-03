package neurevolve;

import neurevolve.network.SigmoidFunction;
import neurevolve.organism.Organism;
import neurevolve.ui.MainWindow;
import neurevolve.world.Space;
import neurevolve.world.World;
import neurevolve.world.WorldConfiguration;

public class Neurevolve {

    public static void main(String[] args) {

        WorldConfiguration config = new WorldConfiguration();
        config.setTemperatureRange(100, 200);
        config.setYear(200, -100);
        config.setMutationRate(20);
        config.setConsumptionRate(20);

        Space frame = new Space(1000, 500);

        World world = new World(new SigmoidFunction(1000), frame, config);
        world.addHill(frame.position(100, 400), 40, 3, 50);
        world.addHill(frame.position(200, 150), 60, 2, 40);
        world.addHill(frame.position(400, 250), 150, 1, 0);
        world.addHill(frame.position(650, 350), 30, 4, 100);
        world.addHill(frame.position(700, 200), 60, 2, 10);
        world.addHill(frame.position(850, 120), 90, 2, 40);
        world.addHill(frame.position(900, 400), 70, 3, 40);
        MainWindow window = new MainWindow(world, frame, config);
        window.show();

        while (world.getTime() < 50000) {
            world.tick();
            if (world.getTime() % 100 == 0) {
                Organism mostComplex = world.getMostComplexOrganism();
                System.out.print(" Pop " + world.getPopulationSize());
                System.out.print(" Complexity " + String.format("%.2f", world.getAverageComplexity()));
                if (mostComplex != null)
                    System.out.print(" Leader " + mostComplex.complexity() + " :" + mostComplex);
                System.out.println();
            }
        }
    }
}
