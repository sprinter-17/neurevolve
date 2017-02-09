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
import neurevolve.maker.WorldMaker;
import neurevolve.world.Space;
import neurevolve.world.WorldConfiguration;

public class NewWorldDialog extends JDialog {

    private final GridBagConstraints pos = new GridBagConstraints();
    private final JPanel optionPanel = new JPanel(new GridBagLayout());
    private Supplier<Integer> width;
    private Supplier<Integer> height;
    private Supplier<Integer> hillCount;
    private Supplier<Integer> hillRadius;
    private Supplier<Integer> hillElevation;
    private Supplier<Integer> startingResources;

    public NewWorldDialog() {
        createUI();
    }

    private void createUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setTitle("Create New World");

        width = addSpinner("Width", 800, 100, 2000, 50);
        height = addSpinner("Height", 500, 100, 1000, 50);
        hillCount = addSpinner("Hill Count", 150, 0, 1000, 10);
        hillRadius = addSpinner("Max Radius", 30, 10, 500, 10);
        hillElevation = addSpinner("Max Elevation", 200, 10, 250, 10);
        startingResources = addSpinner("Starting Resources", 50, 0, 200, 10);

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
        Space space = new Space(width.get(), height.get());
        WorldConfiguration config = new WorldConfiguration();
        WorldMaker maker = new WorldMaker(space, config);
        maker.add(maker.atStart(), maker.elevation(200), maker.verticalEdges(50));
        maker.add(maker.atStart(), maker.acid(), maker.horizontalEdges(40));
        maker.add(maker.atStart(), maker.addResources(150), maker.everywhere());
        maker.add(maker.atStart(), maker.addResources(60), maker.pools(10, 100));
        maker.add(maker.atStart(), maker.elevation(250), maker.pools(1000, 10));
        MainWindow window = new MainWindow(maker.make(), space, config, this);
        window.show();
    }

}
