package neurevolve.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import static javax.swing.SwingWorker.StateValue.DONE;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import neurevolve.organism.Organism;
import neurevolve.organism.Species;
import neurevolve.world.World;

/**
 * The <code>AnalysisWindow</code> is used to analyse the world's current population. It groups the
 * organisms into Species by comparing the distances between their recipes. The user can set the
 * maximum distance that defines the boundaries of a species.
 *
 * The window displays a table of the species with columns for the size (number of members), max
 * (maximum distance between any member and the first) and a textual description of the recipe of
 * the first member.
 */
public class AnalysisWindow extends JFrame {

    private final World world;
    private final SpeciesTableModel speciesModel = new SpeciesTableModel();
    private final List<Consumer<Species>> selectionListeners = new ArrayList<>();

    private Optional<AnalysisWorker> worker = Optional.empty();
    private NetworkPanel networkPanel;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JTable speciesTable;

    /**
     * A table model that displays a list of species
     */
    private static class SpeciesTableModel extends AbstractTableModel {

        private final List<Species> speciesList = new ArrayList<>();

        private enum Column {
            COLOUR("Colour", Integer.class, Species::getColour),
            SIZE("Size", Integer.class, Species::getSize),
            OLDEST("Oldest", Integer.class, Species::getMaxAge),
            AVERAGE_AGE("Average Age", Float.class, Species::getAverageAge),
            COMPLEXITY("Complexity", Integer.class, Species::getComplexity);

            private final String columnName;
            private final Class columnClass;
            private final Function<Species, Object> getter;

            private Column(String columnName, Class columnClass, Function<Species, Object> getter) {
                this.columnName = columnName;
                this.columnClass = columnClass;
                this.getter = getter;
            }
        }

        public void add(List<Species> newSpecies) {
            newSpecies.stream()
                    .filter(s -> !speciesList.contains(s))
                    .forEach(speciesList::add);
            fireTableDataChanged();
        }

        public Species getSpecies(int index) {
            return speciesList.get(index);
        }

        public void clear() {
            speciesList.clear();
            fireTableDataChanged();
        }

        public void setColumns(TableColumnModel columns) {
            getColumn(columns, Column.COLOUR).setCellRenderer(getColourCellRenderer());
            getColumn(columns, Column.COLOUR).setMaxWidth(50);
            getColumn(columns, Column.SIZE).setMaxWidth(100);
            getColumn(columns, Column.OLDEST).setMaxWidth(100);
            getColumn(columns, Column.AVERAGE_AGE).setMaxWidth(100);
            getColumn(columns, Column.AVERAGE_AGE).setCellRenderer(getAgeCellRenderer());
            getColumn(columns, Column.COMPLEXITY).setMaxWidth(100);
        }

        private TableColumn getColumn(TableColumnModel model, Column column) {
            return model.getColumn(column.ordinal());
        }

        @Override
        public int getRowCount() {
            return speciesList.size();
        }

        @Override
        public int getColumnCount() {
            return Column.values().length;
        }

        @Override
        public String getColumnName(int column) {
            return Column.values()[column].columnName;
        }

        @Override
        public Object getValueAt(int row, int column) {
            return Column.values()[column].getter.apply(speciesList.get(row));
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return Column.values()[column].columnClass;
        }

        public TableCellRenderer getColourCellRenderer() {
            return new DefaultTableCellRenderer() {
                private JLabel label;

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    label = (JLabel) super.getTableCellRendererComponent(table, value,
                            isSelected, hasFocus, row, column);
                    return label;
                }

                @Override
                protected void setValue(Object value) {
                    if (label != null && value != null && value instanceof Number) {
                        label.setBackground(new Color(((Number) value).intValue()));
                    }
                    super.setValue("");
                }
            };
        }

        public TableCellRenderer getAgeCellRenderer() {
            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
                @Override
                protected void setValue(Object value) {
                    if (value != null && value instanceof Number) {
                        NumberFormat format = NumberFormat.getNumberInstance();
                        format.setMaximumFractionDigits(1);
                        format.setMinimumFractionDigits(1);
                        value = format.format(value);
                    }
                    super.setValue(value);
                }
            };
            renderer.setHorizontalAlignment(SwingConstants.RIGHT);
            return renderer;
        }
    }

    /**
     * A SwingWorker that analyses a population on a worker thread and progressively updates the
     * table
     */
    private class AnalysisWorker extends SwingWorker<Integer, Species> {

        private final List<Species> speciesList = new ArrayList<>();
        private int populationSize;
        private int processed = 0;

        @Override
        protected Integer doInBackground() {
            populationSize = world.getPopulationSize();
            world.getOrganisms().forEach(this::analyseOrganism);
            return speciesList.size();
        }

        private void analyseOrganism(Organism organism) {
            publish(Species.addToSpecies(organism, speciesList));
            processed++;
        }

        @Override
        protected void process(List<Species> chunks) {
            speciesModel.add(chunks);
            progressBar.setValue(100 * processed / populationSize);
        }

        @Override
        protected void done() {
            statusLabel.setText("Complete");
        }
    }

    /**
     * Create an analysis window for a world.
     *
     * @param world the world to analyse.
     */
    public AnalysisWindow(World world) {
        super("Analysis");
        this.world = world;
        makeToolBar();
        makePopulationTable();
        makeNetworkPanel();
        makeStatusBar();
        pack();
    }

    /**
     * Construct a tool bar
     */
    private void makeToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setBorder(BorderFactory.createEtchedBorder());
        toolBar.add(new AbstractAction("Update") {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        toolBar.add(new AbstractAction("Close") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                selectionListeners.forEach(l -> l.accept(null));
            }
        });
        add(toolBar, BorderLayout.NORTH);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b)
            update();
    }

    private void update() {
        if (!worker.isPresent() || worker.get().getState() == DONE) {
            speciesModel.clear();
            statusLabel.setText("Processing");
            worker = Optional.of(new AnalysisWorker());
            worker.get().execute();
        }
    }

    /**
     * Construct the table to display species
     */
    private void makePopulationTable() {
        speciesTable = new JTable(speciesModel);
        speciesTable.setAutoCreateRowSorter(true);
        speciesModel.setColumns(speciesTable.getColumnModel());
        speciesTable.getSelectionModel().addListSelectionListener(this::selectSpecies);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(speciesTable), BorderLayout.CENTER);
        add(panel, BorderLayout.WEST);
    }

    private void selectSpecies(ListSelectionEvent ev) {
        int row = speciesTable.getSelectedRow();
        if (row >= 0) {
            int index = speciesTable.convertRowIndexToModel(row);
            Species species = speciesModel.getSpecies(index);
            networkPanel.showSpecies(species);
            selectionListeners.forEach(l -> l.accept(species));
        } else {
            selectionListeners.forEach(l -> l.accept(null));
            networkPanel.clear();
        }
    }

    private void makeNetworkPanel() {
        networkPanel = new NetworkPanel(world);
        add(networkPanel, BorderLayout.CENTER);
    }

    /**
     * Construct a status bar to show the status and progress of analysis
     */
    private void makeStatusBar() {
        JPanel statusBar = new JPanel();
        statusLabel = new JLabel("Waiting");
        statusBar.add(statusLabel);
        progressBar = new JProgressBar();
        statusBar.add(progressBar);
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        add(statusBar, BorderLayout.SOUTH);
    }

    public void addSelectionListener(Consumer<Species> listener) {
        selectionListeners.add(listener);
    }
}
