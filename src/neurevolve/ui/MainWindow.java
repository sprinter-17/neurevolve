package neurevolve.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import neurevolve.organism.Organism;
import neurevolve.world.Space;
import neurevolve.world.World;
import neurevolve.world.WorldConfiguration;

/**
 * The main frame for displaying evolving organisms within the world.
 */
public class MainWindow {

    private final NewWorldDialog newWorldDialog;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final World world;
    private final JFrame frame;
    private final JLabel seasonLabel = new JLabel();
    private final JLabel populationLabel = new JLabel();
    private final JLabel averageComplexityLabel = new JLabel();

    private boolean paused = false;
    private int delay = 1;

    /**
     * Construct a frame for displaying a world
     *
     * @param world the world to display
     * @param space the frame for the world
     * @param config the configuration for this world
     * @param newWorldDialog the dialog to use to create new worlds
     */
    public MainWindow(final World world, final Space space, final WorldConfiguration config, NewWorldDialog newWorldDialog) {
        this.world = world;
        this.newWorldDialog = newWorldDialog;
        frame = new JFrame("Neurevolve");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addTools(world, config);
        addMapPanel(world, space, config);
        addConfigPanel(space, config);
        addStatusBar(world);
        frame.pack();
        scheduleTick();
    }

    private void addTools(final World world, final WorldConfiguration config) {
        JPanel tools = new JPanel();
        tools.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        tools.add(new JButton(new AbstractAction("New World") {
            @Override
            public void actionPerformed(ActionEvent e) {
                executor.shutdownNow();
                newWorldDialog.setVisible(true);
                frame.setVisible(false);
            }
        }));
        tools.add(new JButton(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                config.write();
                executor.shutdown();
                System.exit(0);
            }
        }));

        tools.add(new JToggleButton(new AbstractAction("Pause") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (paused)
                    scheduleTick();
                paused = !paused;
            }
        }));

        JButton analysisButton = new JButton();
        analysisButton.setAction(new AbstractAction("Analysis") {
            @Override
            public void actionPerformed(ActionEvent e) {
                analysisButton.setEnabled(false);
                SwingWorker worker = new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        return world.getSpecies(1000, 50);
                    }

                    @Override
                    protected void done() {
                        try {
                            List<List<Organism>> populations = (List<List<Organism>>) get();
                            JFrame analysisFrame = new JFrame("Population Analysis");
                            JTextArea text = new JTextArea();
                            text.setPreferredSize(new Dimension(600, 600));
                            populations.stream()
                                    .sorted(Comparator.comparingInt((List l) -> l.size()).reversed())
                                    .limit(10)
                                    .forEach(pop -> text.append(String.format("%2.2f%%\t%s\n", 100f * pop.size() / 1000, pop.get(0))));
                            analysisFrame.getContentPane().add(text, BorderLayout.CENTER);
                            analysisFrame.pack();
                            analysisFrame.setLocationRelativeTo(null);
                            analysisFrame.setVisible(true);
                            analysisButton.setEnabled(true);
                        } catch (InterruptedException | ExecutionException ex) {
                            ex.printStackTrace(System.out);
                        }
                    }
                };
                worker.execute();
            }
        });
        tools.add(analysisButton);

        JSlider delaySlider = new JSlider(1, 200, delay);
        delaySlider.addChangeListener(ev -> delay = delaySlider.getValue());

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

    private void scheduleTick() {
        executor.schedule(this::tick, delay, TimeUnit.MILLISECONDS);
    }

    private void tick() {
        try {
            world.tick();
            if (!paused)
                scheduleTick();
        } catch (Exception exception) {
            exception.printStackTrace(System.out);
        }
    }
}
