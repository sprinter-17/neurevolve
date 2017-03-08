package neurevolve.ui;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.function.Function;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import neurevolve.world.World;
import neurevolve.world.WorldStatistics;
import neurevolve.world.WorldTicker;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.plot.CategoryMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class TrendWindow extends JFrame {

    private enum Data {
        POPULATION(WorldStatistics::getPopulation),
        COMPLEXITY(WorldStatistics::getAverageComplexity),
        AGE(WorldStatistics::getAverageAge),
        ENERGY(WorldStatistics::getAverageEnergy),
        DESCENDENTS(WorldStatistics::getAverageDescendents),
        SIZE(WorldStatistics::getAverageSize);

        private final Function<WorldStatistics, Float> getter;
        private final DefaultCategoryDataset tickData = new DefaultCategoryDataset();
        private final DefaultCategoryDataset yearData = new DefaultCategoryDataset();

        private Data(Function<WorldStatistics, Float> getter) {
            this.getter = getter;
        }

        public String capitalisedName() {
            return name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
        }

        public void addData(WorldStatistics stats) {
            Integer time = stats.getTime();
            if (tickData.getColumnCount() > 2000)
                tickData.removeColumn(0);
            tickData.addValue(getter.apply(stats), name(), time);
            if (stats.isEndOfYear()) {
                Integer year = stats.getYear();
                yearData.addValue(getter.apply(stats), name(), year);
            }
        }

    }

    private final JToolBar toolBar = new JToolBar();
    private final JPanel chartPanel = new JPanel();
    private final AxisSpace leftSpace = new AxisSpace();

    public TrendWindow(World world, WorldTicker ticker) throws HeadlessException {
        super("Trend Chart");
        chartPanel.setLayout(new BoxLayout(chartPanel, BoxLayout.PAGE_AXIS));
        toolBar.add(new AbstractAction("Close") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        add(toolBar, BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);

        leftSpace.setLeft(50);
        Arrays.stream(Data.values()).forEach(d -> addChart(d, world, ticker));
        pack();
    }

    private void addChart(Data data, World world, WorldTicker ticker) {
        boolean selected = data == Data.POPULATION;
        JPanel chartRow = new JPanel();
        chartRow.setLayout(new BoxLayout(chartRow, BoxLayout.LINE_AXIS));
        chartPanel.add(chartRow);

        JFreeChart tickChart = ChartFactory.createLineChart(null, null, data.name(), data.tickData,
                PlotOrientation.VERTICAL, false, true, false);
        tickChart.getCategoryPlot().setFixedRangeAxisSpace(leftSpace);
        tickChart.getCategoryPlot().getDomainAxis().setVisible(false);
        chartRow.add(new ChartPanel(tickChart, true));

        JFreeChart yearChart = ChartFactory.createLineChart(null, null, data.name(), data.yearData,
                PlotOrientation.VERTICAL, false, true, false);
        yearChart.getCategoryPlot().setFixedRangeAxisSpace(leftSpace);
        yearChart.getCategoryPlot().getDomainAxis().setVisible(false);
        chartRow.add(new ChartPanel(yearChart, true));

        ticker.addTickListener(() -> {
            WorldStatistics stats = ticker.getStats();
            SwingUtilities.invokeLater(() -> {
                data.addData(stats);
                if (stats.isEndOfYear())
                    tickChart.getCategoryPlot().addDomainMarker(new CategoryMarker(stats.getTime()));
            });
        });

        chartRow.setVisible(selected);

        JToggleButton visibilityButton = new JToggleButton(data.name(), selected);
        visibilityButton.setAction(new AbstractAction(data.capitalisedName()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                chartRow.setVisible(visibilityButton.isSelected());
            }
        });
        toolBar.add(visibilityButton);
    }
}
