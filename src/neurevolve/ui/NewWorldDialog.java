package neurevolve.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Vector;
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
import javax.swing.SpinnerNumberModel;
import neurevolve.maker.Loader;
import neurevolve.maker.WorldMaker;
import neurevolve.organism.Recipe;
import neurevolve.world.RecipeSaver;
import neurevolve.world.Space;
import neurevolve.world.World;
import neurevolve.world.WorldConfiguration;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class NewWorldDialog extends JDialog {

    private final GridBagConstraints pos = new GridBagConstraints();
    private final JPanel optionPanel = new JPanel(new GridBagLayout());
    private final Loader loader = new Loader();
    private Optional<Path> recipe = Optional.empty();
    private Supplier<Integer> width;
    private Supplier<Integer> height;

    public NewWorldDialog() {
        createUI();
    }

    private void createUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setTitle("Create New World");

        optionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createEtchedBorder()));

        pos.insets.left = 5;
        pos.insets.right = 5;
        pos.insets.bottom = 5;

        width = addSpinner("Width", 800, 100, 2000, 50);
        height = addSpinner("Height", 500, 100, 1000, 50);

        Vector<String> recipeFiles = new Vector<>();
        recipeFiles.add("none");
        try {
            Files.newDirectoryStream(Paths.get("recipes"))
                    .forEach(path -> recipeFiles.add(path.toString()));
        } catch (IOException ex) {
            Logger.getLogger(NewWorldDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        JComboBox<String> recipeCombo = new JComboBox(recipeFiles);
        addComponent("Seed Recipe", recipeCombo);
        recipeCombo.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (recipeCombo.getSelectedIndex() == 0)
                    recipe = Optional.empty();
                else
                    recipe = Optional.of(Paths.get(recipeCombo.getItemAt(recipeCombo.getSelectedIndex())));
            }
        });

        Vector<String> worldFiles = new Vector<>();
        try {
            Files.newDirectoryStream(Paths.get("worlds"))
                    .forEach(path -> worldFiles.add(path.toString()));
        } catch (IOException ex) {
            Logger.getLogger(NewWorldDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        JComboBox<String> worldCombo = new JComboBox(worldFiles);
        addComponent("World", worldCombo);
        JTextArea description = new JTextArea();
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setRows(5);
        pos.gridwidth = 2;
        pos.gridx = 1;
        optionPanel.add(description, pos);

        worldCombo.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String fileName = worldCombo.getItemAt(worldCombo.getSelectedIndex());
                    InputSource source = new InputSource(new FileInputStream(fileName));
                    Space space = new Space(width.get(), height.get());
                    WorldConfiguration config = new WorldConfiguration();
                    WorldMaker maker = new WorldMaker(space, config);
                    loader.load(maker, config, fileName, source);
                    description.setText(loader.getDescription().orElse(" "));
                } catch (FileNotFoundException | SAXException ex) {
                    description.setText(ex.getMessage());
                }
            }
        });

        add(optionPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEtchedBorder());
        buttonPanel.add(new JButton(new AbstractAction("Create World") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String fileName = worldCombo.getItemAt(worldCombo.getSelectedIndex());
                    InputSource source = new InputSource(new FileInputStream(fileName));
                    Space space = new Space(width.get(), height.get());
                    WorldConfiguration config = new WorldConfiguration();
                    WorldMaker maker = new WorldMaker(space, config);
                    loader.load(maker, config, fileName, source);
                    World world = maker.make();
                    if (recipe.isPresent()) {
                        RecipeSaver loader = new RecipeSaver(world);
                        InputSource input = new InputSource(Files.newInputStream(recipe.get()));
                        Recipe seedRecipe = loader.load(input);
                        config.setSeedRecipe(seedRecipe);
                    }
                    MainWindow window = new MainWindow(loader.getName(), world, space,
                            config, NewWorldDialog.this);
                    window.show();
                    setVisible(false);
                } catch (IOException | SAXException ex) {
                    description.setText(ex.getMessage());
                }
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
        SpinnerNumberModel model = new SpinnerNumberModel(initialValue, min, max, step);
        addComponent(label, new JSpinner(model));
        return () -> model.getNumber().intValue();
    }

    private void addComponent(String label, Component component) {
        pos.gridx = 1;
        pos.anchor = GridBagConstraints.EAST;
        pos.fill = GridBagConstraints.HORIZONTAL;
        optionPanel.add(new JLabel(label), pos);

        pos.gridx = 2;
        pos.anchor = GridBagConstraints.WEST;
        pos.fill = GridBagConstraints.HORIZONTAL;
        optionPanel.add(component, pos);
    }
}
