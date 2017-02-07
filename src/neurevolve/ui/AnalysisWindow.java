package neurevolve.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import static javax.swing.SwingWorker.StateValue.DONE;
import javax.swing.table.AbstractTableModel;
import neurevolve.organism.Organism;
import neurevolve.organism.Species;
import neurevolve.world.World;

/**
 * The <code>AnalysisWindow</code> is used to analyse the world's current population. It attempts to
 * group the organisms into {@link neurevolve.organism.Species} by comparing the distances between
 * their recipes. The user can set the maximum distance that defines the boundaries of a species.
 *
 * The window displays a table of the species with columns for the size (number of members), max
 * (maximum distance between any member and the first) and a textual description of the recipe of
 * the first member.
 */
public class AnalysisWindow extends JFrame {

    private final World world;
    private final SpeciesTableModel speciesTable = new SpeciesTableModel();
    private Optional<AnalysisWorker> worker = Optional.empty();
    private JProgressBar progressBar;
    private JLabel statusLabel;

    private static class SpeciesTableModel extends AbstractTableModel {

        private final List<Species> speciesList = new ArrayList<>();

        private enum Column {
            SIZE("Size", Integer.class, Species::size),
            DISTANCE("Width", Integer.class, Species::getLargestDistance),
            RECIPE("Recipe", String.class, Species::toString);

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
            for (Species species : newSpecies) {
                if (!speciesList.contains(species))
                    speciesList.add(species);
            }
            fireTableDataChanged();
        }

        public void clear() {
            speciesList.clear();
            fireTableDataChanged();
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
    }

    private class AnalysisWorker extends SwingWorker<Integer, Species> {

        private final int maxDistance;
        private final List<Species> speciesList = new ArrayList<>();
        private int populationSize;
        private int processed = 0;

        public AnalysisWorker(int maxDistance) {
            this.maxDistance = maxDistance;
        }

        @Override
        protected Integer doInBackground() {
            populationSize = world.getPopulationSize();
            world.getOrganisms().forEach(this::analyseOrganism);
            return speciesList.size();
        }

        private void analyseOrganism(Organism organism) {
            publish(Species.addToSpecies(organism, speciesList, maxDistance));
            processed++;
        }

        @Override
        protected void process(List<Species> chunks) {
            speciesTable.add(chunks);
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
        makeStatusBar();
        pack();
    }

    private void makeToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setBorder(BorderFactory.createEtchedBorder());
        toolBar.add(new JLabel("Species Definer"));
        JSlider distanceSlider = new JSlider(1, 100, 20);
        toolBar.add(distanceSlider);
        toolBar.add(new AbstractAction("Update") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!worker.isPresent() || worker.get().getState() == DONE) {
                    statusLabel.setText("Processing");
                    speciesTable.clear();
                    worker = Optional.of(new AnalysisWorker(distanceSlider.getValue()));
                    worker.get().execute();
                }
            }
        });
        toolBar.add(new AbstractAction("Close") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        add(toolBar, BorderLayout.NORTH);
    }

    private void makePopulationTable() {
        JTable table = new JTable(speciesTable);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setMaxWidth(50);
        table.setPreferredSize(new Dimension(600, 600));
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void makeStatusBar() {
        JPanel statusBar = new JPanel();
        statusLabel = new JLabel("Waiting");
        statusBar.add(statusLabel);
        progressBar = new JProgressBar();
        statusBar.add(progressBar);
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        add(statusBar, BorderLayout.SOUTH);
    }
}
