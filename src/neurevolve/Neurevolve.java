package neurevolve;

import neurevolve.network.SigmoidFunction;
import neurevolve.organism.Instruction;
import neurevolve.organism.Organism;
import neurevolve.organism.Recipe;
import neurevolve.organism.RecipePrinter;
import neurevolve.world.World;
import neurevolve.world.WorldActivity;
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
        Recipe recipe = new Recipe();
        recipe.add(Instruction.ADD_NEURON, 0);
        recipe.add(Instruction.SET_ACTIVITY, WorldActivity.EAT.ordinal());
        recipe.add(Instruction.ADD_NEURON, 0);
        recipe.add(Instruction.SET_ACTIVITY, WorldActivity.DIVIDE.ordinal());

        World world = new World(new SigmoidFunction(1000), 800, 800);
        world.setTemperatureRange(30, 90);
        world.setYear(100, -110);
        world.seed(recipe, 1000);
        world.setMutationRate(10);

        for (int t = 0; t < 1000; t++) {
            world.tick();
            Organism mostComplex = world.getLargestOrganism();
            System.out.println(world.getTime() + ":" + world.getPopulationSize()
                    + " (" + String.format("%.2f", world.getAverageComplexity()) + ")"
                    + " FM: " + Runtime.getRuntime().freeMemory()
                    + " ^ " + mostComplex.complexity() + ":" + mostComplex.toString(PRINTER));
        }
    }
}
