package neurevolve;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.util.ArrayList;
import javax.swing.JFrame;
import neurevolve.network.SigmoidFunction;
import neurevolve.organism.Instruction;
import neurevolve.organism.Organism;
import neurevolve.organism.Recipe;
import neurevolve.organism.Species;
import neurevolve.ui.NetworkPanel;
import neurevolve.ui.NewWorldDialog;
import neurevolve.world.Space;
import neurevolve.world.World;
import neurevolve.world.WorldActivity;
import neurevolve.world.WorldConfiguration;
import neurevolve.world.WorldInput;

/**
 * Neurevolve simulates natural evolution by tracking the development of complex behaviours in
 * simple organisms in a resource-limited world. The behaviour of organisms is controlled by simple
 * neural networks which are, in turn, created by recipes. The recipes are copied during replication
 * with random transcription errors introduced.
 *
 * The user interface for Neurevolve consists of four windows:
 * <ul>
 * <li>The {@link neurevolve.ui.NewWorldDialog} allows the user to select values for the creation of
 * the world.</li>
 * <li>The {@link neurevolve.ui.MainWindow} displays a map of the world as the organisms develop and
 * allows the user to change the configuration of the world.</li>
 * <li>The {@link neurevolve.ui.ZoomWindow} allows the user to view an expanded view of a section of
 * the world.</li>
 * <li>The {@link neurevolve.ui.AnalysisWindow} performs a statistical sampling of the population to
 * help understand the behaviour of individual organisms.</li>
 * </ul>
 *
 *
 */
public class Neurevolve {

    public static void main(String[] args) {
        new NewWorldDialog().setVisible(true);
//        testNetworkFrame();
    }

    private static void testNetworkFrame() throws HeadlessException {
        Space space = new Space(200, 200);
        WorldConfiguration config = new WorldConfiguration();
        World world = new World(new SigmoidFunction(100), space, config);
        Recipe recipe = new Recipe(345);

        recipe.add(Instruction.ADD_NEURON, -4);
        recipe.add(Instruction.SET_ACTIVITY, WorldActivity.TURN_LEFT.ordinal());
        recipe.add(Instruction.ADD_INPUT, WorldInput.AGE.ordinal(), 5);

        recipe.add(Instruction.ADD_NEURON, +7);

        recipe.add(Instruction.ADD_NEURON, 0);
        recipe.add(Instruction.SET_ACTIVITY, WorldActivity.TURN_RIGHT.ordinal());

        recipe.add(Instruction.ADD_NEURON, 0);
        recipe.add(Instruction.ADD_LINK, 0, -4);
        recipe.add(Instruction.SET_ACTIVITY, WorldActivity.MOVE.ordinal());

        recipe.add(Instruction.ADD_NEURON, 0);
        recipe.add(Instruction.ADD_INPUT, WorldInput.AGE.ordinal(), 0);
        recipe.add(Instruction.ADD_LINK, 3, 0);
        recipe.add(Instruction.SET_ACTIVITY, WorldActivity.DIVIDE.ordinal());

        recipe.add(Instruction.ADD_NEURON, 0);
        recipe.add(Instruction.ADD_LINK, 2, 4);
        recipe.add(Instruction.ADD_INPUT, WorldInput.AGE.ordinal(), -7);
        recipe.add(Instruction.ADD_INPUT, WorldInput.LOOK_SPACE_RIGHT.ordinal(), -7);
        recipe.add(Instruction.ADD_LINK, 3, -1);
        recipe.add(Instruction.SET_ACTIVITY, WorldActivity.EAT_FORWARD.ordinal());

        recipe.add(Instruction.ADD_NEURON, 0);
        recipe.add(Instruction.ADD_LINK, 4, -11);
        recipe.add(Instruction.SET_ACTIVITY, WorldActivity.EAT_HERE.ordinal());

        recipe.add(Instruction.ADD_NEURON, 0);
        recipe.add(Instruction.SET_ACTIVITY, WorldActivity.ATTACK.ordinal());

        recipe.add(Instruction.ADD_NEURON, -24);
        recipe.add(Instruction.ADD_LINK, 0, 31);

        Organism organism = new Organism(world, 100, recipe);
        JFrame frame = new JFrame("Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        NetworkPanel panel = new NetworkPanel();
        panel.showSpecies(Species.addToSpecies(organism, new ArrayList<>()));
        panel.setPreferredSize(new Dimension(400, 400));
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
        panel.repaint();
    }
}
