package neurevolve;

import neurevolve.network.SigmoidFunction;
import neurevolve.organism.Instruction;
import neurevolve.organism.Recipe;
import neurevolve.world.World;
import neurevolve.world.WorldActivity;

public class Neurevolve {

    public static void main(String[] args) {
        Recipe recipe = new Recipe();
        recipe.add(Instruction.ADD_NEURON, -100);
        recipe.add(Instruction.SET_ACTIVITY, WorldActivity.EAT.ordinal());
        recipe.add(Instruction.ADD_NEURON, -100);
        recipe.add(Instruction.SET_ACTIVITY, WorldActivity.DIVIDE.ordinal());

        World world = new World(new SigmoidFunction(1000), 500, 500);
        world.setTemperatureRange(0, 90);
        world.setYear(100, -90);
        world.seed(recipe, 1000);
        world.setMutationRate(10);

        for (int t = 0; t < 1000; t++) {
            world.tick();
            System.out.println(world.getTime() + ":" + world.getPopulationSize() + " ^ " + world.getLargestOrganism().size());
        }
    }
}
