package neurevolve.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import neurevolve.organism.Instruction;
import neurevolve.organism.Recipe;
import neurevolve.world.Space;
import neurevolve.world.World;
import neurevolve.world.WorldActivity;
import neurevolve.world.WorldConfiguration;

/**
 * The main frame for displaying evolving organisms within the world.
 */
public class MainWindow {

    private final JFrame frame;
    private final JLabel seasonLabel = new JLabel();
    private final JLabel populationLabel = new JLabel();
    private final JLabel averageComplexityLabel = new JLabel();

    /**
     * Construct a frame for displaying a world
     *
     * @param world the world to display
     * @param space the frame for the world
     * @param config the configuration for this world
     */
    public MainWindow(final World world, final Space space, final WorldConfiguration config) {
        frame = new JFrame("Neurevolve");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addTools(world);
        addMapPanel(world, space, config);
        addConfigPanel(space, config);
        addStatusBar(world);
        frame.pack();
    }

    private void addTools(final World world) {
        JPanel tools = new JPanel();
        tools.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        tools.add(new JButton(new AbstractAction("Seed") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Recipe recipe = new Recipe();
                recipe.add(Instruction.ADD_NEURON, 0);
                recipe.add(Instruction.SET_ACTIVITY, WorldActivity.EAT_HERE.ordinal());
                recipe.add(Instruction.ADD_NEURON, 0);
                recipe.add(Instruction.SET_ACTIVITY, WorldActivity.DIVIDE.ordinal());
                world.seed(recipe, 1000, 100);
            }
        }));
        tools.add(new JButton(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        }));
        
        JSlider delaySlider = new JSlider(1, 100, world.getDelay());
        delaySlider.addChangeListener((ChangeEvent e) -> {
            world.setDelay(delaySlider.getValue());
        });
        
        tools.add(new JLabel("Delay (ms)"));
        tools.add(new JLabel(String.valueOf(delaySlider.getMinimum())));
        tools.add(delaySlider);
        tools.add(new JLabel(String.valueOf(delaySlider.getMaximum())));
        
        frame.getContentPane().add(tools, BorderLayout.NORTH);
    }

    private void addMapPanel(final World world, final Space space, final WorldConfiguration config) {
        MapPanel mapPanel = new MapPanel(world, space, config);
        mapPanel.setPreferredSize(new Dimension(space.getWidth(), space.getHeight()));
        frame.getContentPane().add(mapPanel, BorderLayout.CENTER);
    }

    private void addConfigPanel(Space space, final WorldConfiguration config) {
        ConfigPanel configPanel = new ConfigPanel(config);
        configPanel.setPreferredSize(new Dimension(300, space.getHeight()));
        frame.getContentPane().add(configPanel, BorderLayout.EAST);
    }

    private void addStatusBar(final World world) {
        JPanel statusBar = new JPanel();
        statusBar.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        statusBar.add(seasonLabel);
        statusBar.add(populationLabel);
        statusBar.add(averageComplexityLabel);
        Timer timer = new Timer(10, ev -> updateLabels(world));
        timer.start();
        frame.getContentPane().add(statusBar, BorderLayout.SOUTH);
    }

    private void updateLabels(World world) {
        seasonLabel.setText(world.getSeasonName());
        populationLabel.setText(Integer.toString(world.getPopulationSize()));
    }

    /**
     * Show the frame
     */
    public void show() {
        frame.setVisible(true);
    }
}
