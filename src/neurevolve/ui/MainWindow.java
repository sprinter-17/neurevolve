package neurevolve.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import neurevolve.world.Configuration;
import neurevolve.world.Space;
import neurevolve.world.World;
import neurevolve.world.WorldTicker;

/**
 * The main frame for displaying evolving organisms within the world.
 */
public class MainWindow {

    private static final Logger LOG = Logger.getLogger(MainWindow.class.getName());

    private final NewWorldDialog newWorldDialog;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final WorldTicker ticker;
    private final JFrame frame;
    private final AnalysisWindow analysisWindow;
    private final TrendWindow trendWindow;
    private final JToggleButton pauseButton = new JToggleButton("Pause");
    private final JLabel seasonLabel = new JLabel();
    private final JLabel populationLabel = new JLabel();
    private final JLabel averageComplexityLabel = new JLabel();

    private boolean paused = false;
    private int delay = 1;

    /**
     * Construct a frame for displaying a world.
     *
     * @param title the text to put in the frame's title bar
     * @param world the world to display
     * @param space the frame for the world
     * @param config the configuration for this world
     * @param newWorldDialog the dialog to use to create new worlds
     */
    public MainWindow(String title, final World world, final WorldTicker ticker, final Space space,
            final Configuration config,
            NewWorldDialog newWorldDialog) {
        this.ticker = ticker;
        this.newWorldDialog = newWorldDialog;
        frame = new JFrame("Neurevolve");
        frame.setTitle(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        analysisWindow = new AnalysisWindow(world);
        trendWindow = new TrendWindow(world, ticker);
        addTools();
        addMapPanel(world, space, config);
        addConfigPanel(space, config);
        addStatusBar(world);
        frame.pack();
        scheduleTick();
    }

    private void addTools() {
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
        tools.add(new JButton(new AbstractAction("Trend") {
            @Override
            public void actionPerformed(ActionEvent e) {
                trendWindow.setVisible(true);
            }
        }));

        JButton analysisButton = new JButton();
        analysisButton.setAction(new AbstractAction("Species") {
            @Override
            public void actionPerformed(ActionEvent e) {
                analysisWindow.setVisible(true);
            }
        });
        tools.add(analysisButton);

        JSlider delaySlider = new JSlider(1, 200, delay);
        delaySlider.addChangeListener(ev -> delay = delaySlider.getValue());

        tools.add(new JLabel("Delay (ms)"));
        tools.add(new JLabel(String.valueOf(delaySlider.getMinimum())));
        tools.add(delaySlider);
        tools.add(new JLabel(String.valueOf(delaySlider.getMaximum())));

        tools.add(pauseButton);
        pauseButton.setAction(new AbstractAction("Pause") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (paused)
                    scheduleTick();
                paused = !paused;
            }
        });

        tools.add(new JButton(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                executor.shutdown();
                System.exit(0);
            }
        }));

        frame.getContentPane().add(tools, BorderLayout.NORTH);
    }

    private void addMapPanel(final World world, final Space space, final Configuration config) {
        MapPanel mapPanel = new MapPanel(world, ticker, space, config);
        mapPanel.setPreferredSize(new Dimension(space.getWidth(), space.getHeight() + 10));
        analysisWindow.addSelectionListener(mapPanel::selectSpecies);
        frame.getContentPane().add(mapPanel, BorderLayout.CENTER);
    }

    private void addConfigPanel(Space space, final Configuration config) {
        ConfigPanel configPanel = new ConfigPanel(config);
        configPanel.setPreferredSize(new Dimension(350, space.getHeight()));
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
        seasonLabel.setText(ticker.getSeasonName());
        populationLabel.setText(Integer.toString(world.getPopulationSize()));
    }

    /**
     * Show the frame
     */
    public void show() {
        frame.setVisible(true);
    }

    private void scheduleTick() {
        if (!executor.isShutdown())
            executor.schedule(this::tick, delay, TimeUnit.MILLISECONDS);
    }

    private void tick() {
        try {
            ticker.tick();
            if (!paused)
                scheduleTick();
        } catch (Exception exception) {
            LOG.log(Level.SEVERE, "Tick exception", exception);
            exception.printStackTrace(System.out);
        }
    }
}
