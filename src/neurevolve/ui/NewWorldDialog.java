package neurevolve.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.function.Supplier;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import neurevolve.network.SigmoidFunction;
import neurevolve.world.Space;
import neurevolve.world.World;
import neurevolve.world.WorldConfiguration;

public class NewWorldDialog extends JDialog {

    private final GridBagConstraints pos = new GridBagConstraints();
    private final JPanel optionPanel = new JPanel(new GridBagLayout());
    private Supplier<Integer> width;
    private Supplier<Integer> height;
    private Supplier<Integer> hillCount;
    private Supplier<Integer> hillRadius;
    private Supplier<Integer> hillElevation;

    public NewWorldDialog() {
        createUI();
    }

    private void createUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setTitle("Create New World");

        width = addSpinner("Width", 600, 100, 2000, 50);
        height = addSpinner("Height", 400, 100, 1000, 50);
        hillCount = addSpinner("Hill Count", 50, 0, 1000, 10);
        hillRadius = addSpinner("Max Radius", 100, 10, 500, 10);
        hillElevation = addSpinner("Max Elevation", 100, 10, 250, 10);

        add(optionPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEtchedBorder());
        buttonPanel.add(new JButton(new AbstractAction("Create World") {
            @Override
            public void actionPerformed(ActionEvent e) {
                createWorld();
                setVisible(false);
            }
        }));
        buttonPanel.add(new JButton(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        }));
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private Supplier<Integer> addSpinner(String label, int initialValue, int min, int max, int step) {
        pos.gridx = 1;
        pos.anchor = GridBagConstraints.EAST;
        pos.fill = GridBagConstraints.HORIZONTAL;
        optionPanel.add(new JLabel(label), pos);

        pos.gridx = 2;
        pos.anchor = GridBagConstraints.WEST;
        pos.fill = GridBagConstraints.HORIZONTAL;
        SpinnerNumberModel model = new SpinnerNumberModel(initialValue, min, max, step);
        JSpinner spinner = new JSpinner(model);
        optionPanel.add(spinner, pos);
        return () -> model.getNumber().intValue();
    }

    private void createWorld() {
        WorldConfiguration config = new WorldConfiguration();
        Space space = new Space(width.get(), height.get());
        World world = new World(new SigmoidFunction(200), space, config);
        world.addHills(hillCount.get(), hillRadius.get(), hillElevation.get());
        MainWindow window = new MainWindow(world, space, config, this);
        window.show();
    }

}
