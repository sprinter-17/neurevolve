package neurevolve;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import neurevolve.network.SigmoidFunction;
import neurevolve.organism.Organism;
import neurevolve.ui.MainWindow;
import neurevolve.world.Space;
import neurevolve.world.World;
import neurevolve.world.WorldActivity;
import neurevolve.world.WorldConfiguration;

public class Neurevolve {

    public static void main(String[] args) {

        WorldConfiguration config = new WorldConfiguration();
        config.setTemperatureRange(50, 150);
        config.setYear(200, -100);
        config.setMutationRate(20);
        config.setConsumptionRate(20);
        config.setActivityCost(WorldActivity.DIVIDE, 5);
        config.setActivityCost(WorldActivity.MOVE, 2);

        Space frame = new Space(600, 600);

        World world = new World(new SigmoidFunction(100), frame, config);
        world.addHills(100, 60);
        MainWindow window = new MainWindow(world, frame, config);
        window.show();

        while (world.getTime() < 500000) {
            tick(world);
            try {
                TimeUnit.MILLISECONDS.sleep(world.getDelay());
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }

    private static void tick(World world) {
        world.tick();
        if (world.getTime() % 100 == 0) {
            Organism mostComplex = world.getMostComplexOrganism();
            System.out.print(" Pop " + world.getPopulationSize());
            System.out.print(" Complexity " + String.format("%.4f", world.getAverageComplexity()));
            if (mostComplex != null) {
                System.out.print(" Leader " + String.format("%.4f", mostComplex.complexity())
                        + " :" + mostComplex);
            }
            System.out.println();
        }
    }
}
