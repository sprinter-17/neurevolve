package neurevolve.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import neurevolve.maker.Loader;
import neurevolve.maker.WorldMaker;
import neurevolve.organism.Recipe;
import static neurevolve.ui.FileComboModel.EXT;
import neurevolve.world.Configuration;
import neurevolve.world.RecipeSaver;
import neurevolve.world.Space;
import neurevolve.world.World;
import neurevolve.world.WorldTicker;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A {@code NewWorldDiaglog} displays the fields that are used to select settings for a new world.
 * The fields include the size of the world's space and files for the seed recipe and the world
 * loader.
 */
public class NewWorldDialog extends JDialog {

    private final GridBagConstraints pos = new GridBagConstraints();
    private final Loader worldLoader = new Loader();
    private final Configuration config = new Configuration();
    private final FileComboModel recipeModel = new FileComboModel("recipes");
    private final FileComboModel worldModel = new FileComboModel("worlds");
    private final JTextArea worldDescription = new JTextArea();
    private Supplier<Integer> width;
    private Supplier<Integer> height;

    /**
     * Construct a new dialog.
     */
    public NewWorldDialog() {
        createUI();
    }

    /**
     * Create the UI components for the dialog;
     */
    private void createUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setTitle("Create New World");
        createOptionPanel();
        createButtonPanel();
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Create the panel containing the world's options
     */
    private void createOptionPanel() {
        JPanel optionPanel = new JPanel(new GridBagLayout());
        optionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createEtchedBorder()));

        pos.insets.left = 5;
        pos.insets.right = 5;
        pos.insets.bottom = 5;

        width = addSpinner(optionPanel, "Width", 800, 100, 2000, 50);
        height = addSpinner(optionPanel, "Height", 500, 100, 1000, 50);

        addFileComboBox(optionPanel, "Seed Recipe", recipeModel);
        JComboBox<Path> worldCombo = addFileComboBox(optionPanel, "World", worldModel);

        worldDescription.setLineWrap(true);
        worldDescription.setWrapStyleWord(true);
        worldDescription.setRows(5);
        pos.gridwidth = 2;
        pos.gridx = 1;
        optionPanel.add(worldDescription, pos);

        worldCombo.addItemListener(ev -> {
            if (ev.getStateChange() == ItemEvent.SELECTED)
                loadWorld();
        });

        add(optionPanel, BorderLayout.CENTER);
    }

    /**
     * Add and return a combobox that allows a file to be selected.
     */
    private JComboBox<Path> addFileComboBox(JPanel panel, String label, FileComboModel model) {
        JComboBox<Path> comboBox = new JComboBox(model);
        comboBox.addPopupMenuListener(model);
        ListCellRenderer renderer = comboBox.getRenderer();
        comboBox.setRenderer((list, path, index, selected, focus) -> {
            String fileName = path == null ? "" : path.getFileName().toString().replace(EXT, "");
            Component component = renderer.getListCellRendererComponent(list, fileName, index,
                    selected, focus);
            return component;
        });
        addComponent(panel, label, comboBox);
        return comboBox;
    }

    /**
     * Add a spinner for a number.
     *
     * @return the method to call to get the spinner's value
     */
    private Supplier<Integer> addSpinner(JPanel panel, String label, int initialValue, int min, int max, int step) {
        SpinnerNumberModel model = new SpinnerNumberModel(initialValue, min, max, step);
        addComponent(panel, label, new JSpinner(model));
        return () -> model.getNumber().intValue();
    }

    /**
     * Add a component with a label
     */
    private void addComponent(JPanel panel, String label, Component component) {
        pos.gridx = 1;
        pos.anchor = GridBagConstraints.EAST;
        pos.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(label), pos);

        pos.gridx = 2;
        pos.anchor = GridBagConstraints.WEST;
        pos.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, pos);
    }

    /**
     * Create the panel with buttons to create the world or exit
     */
    private void createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEtchedBorder());
        buttonPanel.add(new JButton(new AbstractAction("Create World") {
            @Override
            public void actionPerformed(ActionEvent e) {
                createWorld();
            }
        }));
        buttonPanel.add(new JButton(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        }));
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Create and return a {@code WorldMaker} using the file selected in the world combobox
     */
    private WorldMaker loadWorld() {
        try {
            Path path = worldModel.getSelectedPath();
            InputStream fileStream = Files.newInputStream(path);
            InputSource source = new InputSource(fileStream);
            WorldMaker maker = new WorldMaker(getSpace(), config);
            worldLoader.load(maker, config, path.getFileName().toString(), source);
            worldDescription.setText(worldLoader.getDescription().orElse(" "));
            return maker;
        } catch (IOException | SAXException ex) {
            worldDescription.setText(ex.getMessage());
            return null;
        }
    }

    /**
     * Set the seed recipe based on the file selected in the recipe combobox
     */
    private void loadRecipe(World world) {
        if (recipeModel.getSelectedItem() != null) {
            try {
                RecipeSaver recipeLoader = new RecipeSaver(world);
                InputSource input = new InputSource(Files.newInputStream(recipeModel.getSelectedPath()));
                Recipe seedRecipe = recipeLoader.load(input);
                config.setSeedRecipe(seedRecipe);
            } catch (IOException | SAXException ex) {
                Logger.getLogger(NewWorldDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Create a new world and display the main window
     */
    private void createWorld() {
        WorldMaker maker = loadWorld();
        World world = maker.make();
        loadRecipe(world);
        WorldTicker ticker = new WorldTicker(world, config);
        ticker.addTickListener(() -> maker.process(world, ticker.getTime()));
        MainWindow window = new MainWindow(worldLoader.getName(), world, ticker, getSpace(),
                config, NewWorldDialog.this);
        window.show();
        setVisible(false);
    }

    /**
     * Create a new space based on the selected width and height.
     *
     * @return the created space.
     */
    private Space getSpace() {
        return new Space(width.get(), height.get());
    }
}
