package neurevolve.world;

import neurevolve.network.ActivationFunction;
import neurevolve.organism.Environment;
import neurevolve.organism.Organism;

public class WorldEnvironment implements Environment {

    private final World world;
    private final ActivationFunction function;

    private Position position;
    private Organism organism;

    public WorldEnvironment(World world, ActivationFunction function) {
        this.world = world;
        this.function = function;
    }

    public void setContext(Position position, Organism organism) {
        this.position = position;
        this.organism = organism;
    }

    @Override
    public ActivationFunction getActivationFunction() {
        return function;
    }

    @Override
    public int getInput(int input) {
        return WorldInput.getValue(input, world, position);
    }

    @Override
    public void performActivity(int activity) {
        WorldActivity.perform(activity, world, position);
    }

}
