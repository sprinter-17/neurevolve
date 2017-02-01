package neurevolve.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import neurevolve.organism.Instruction;
import neurevolve.organism.Recipe;
import neurevolve.world.Frame;
import neurevolve.world.World;
import neurevolve.world.WorldActivity;
import neurevolve.world.WorldConfiguration;

public class MainWindow {

    private final JFrame frame;
    private final JPanel tools;
    private final MapPanel mapPanel;
    private final JPanel statusBar;
    private final JLabel seasonLabel = new JLabel();
    private final JLabel populationLabel = new JLabel();
    private final JLabel averageComplexityLabel = new JLabel();

    public MainWindow(final World world, final Frame worldFrame, final WorldConfiguration config) {
        frame = new JFrame("Neurevolve");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tools = new JPanel();
        tools.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        tools.add(new JButton(new AbstractAction("Seed") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Recipe recipe = new Recipe();
                recipe.add(Instruction.ADD_NEURON, 0);
                recipe.add(Instruction.SET_ACTIVITY, WorldActivity.EAT.ordinal());
                recipe.add(Instruction.ADD_NEURON, 0);
                recipe.add(Instruction.SET_ACTIVITY, WorldActivity.DIVIDE.ordinal());
                world.seed(recipe, 100);
            }
        }));
        tools.add(new JButton(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        }));
        frame.getContentPane().add(tools, BorderLayout.NORTH);
        mapPanel = new MapPanel(world, worldFrame);
        frame.getContentPane().add(mapPanel, BorderLayout.CENTER);
        statusBar = new JPanel();
        statusBar.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        statusBar.add(seasonLabel);
        statusBar.add(populationLabel);
        statusBar.add(averageComplexityLabel);
        Timer timer = new Timer(10, ev -> updateLabels(world));
        timer.start();
        frame.getContentPane().add(statusBar, BorderLayout.SOUTH);
        frame.pack();
    }

    private void updateLabels(World world) {
        seasonLabel.setText(world.getSeasonName());
        populationLabel.setText(Integer.toString(world.getPopulationSize()));
    }

    public void show() {
        frame.setVisible(true);
    }
}
