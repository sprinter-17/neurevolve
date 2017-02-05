package neurevolve;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import neurevolve.network.SigmoidFunction;
import neurevolve.organism.Organism;
import neurevolve.ui.MainWindow;
import neurevolve.world.Space;
import neurevolve.world.World;
import neurevolve.world.WorldConfiguration;

public class Neurevolve {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final WorldConfiguration config = new WorldConfiguration();
    private final Space space = new Space(800, 600);
    private final World world = new World(new SigmoidFunction(200), space, config);

    private Neurevolve() {
        world.addHills(space.size() / 400, space.getHeight() / 40);
    }

    public static void main(String[] args) {
        Neurevolve neurevolve = new Neurevolve();
        neurevolve.showWindow();
        neurevolve.scheduleTick();
    }

    private void showWindow() {
        MainWindow window = new MainWindow(world, space, config);
        window.show();
    }

    private void scheduleTick() {
        executor.schedule(this::tick, world.getDelay(), TimeUnit.MILLISECONDS);
    }

    private void tick() {
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
        scheduleTick();
    }
}
