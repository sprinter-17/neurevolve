package neurevolve.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.function.Consumer;
import static javax.swing.BorderFactory.createEtchedBorder;
import static javax.swing.BorderFactory.createTitledBorder;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import javax.swing.border.Border;
import neurevolve.world.WorldActivity;
import neurevolve.world.WorldConfiguration;

public class ConfigPanel extends JTabbedPane {

    private final WorldConfiguration config;
    private final GridBagConstraints layout = new GridBagConstraints();

    public ConfigPanel(WorldConfiguration config) {
        super();
        this.config = config;
        layout.insets = new Insets(0, 8, 0, 8);
        addWorldPanel();
        addOrganismPanel();
    }

    private void addWorldPanel() {
        JPanel worldPanel = new JPanel();
        LayoutManager panelLayout = new BoxLayout(worldPanel, BoxLayout.Y_AXIS);
        worldPanel.setLayout(panelLayout);
        addTab("World", new JScrollPane(worldPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER));
        layout.gridy = 0;
        JPanel mutationPanel = addGroupPanel(worldPanel, "Mutation");
        addValueSlider(mutationPanel, "Rate", 0, 100, config.getMutationRate(), config::setMutationRate);

        JPanel temperaturePanel = addGroupPanel(worldPanel, "Temperature");
        JSlider minTemp = addValueSlider(temperaturePanel, "At Pole", -200, 400, config.getMinTemp(), (v) -> config.setTemperatureRange(v, config.getMaxTemp()));
        JSlider maxTemp = addValueSlider(temperaturePanel, "At Equator", -200, 400, config.getMaxTemp(), (v) -> config.setTemperatureRange(config.getMinTemp(), v));
        minTemp.addChangeListener(e -> {
            if (minTemp.getValue() > maxTemp.getValue())
                maxTemp.setValue(minTemp.getValue());
        });
        maxTemp.addChangeListener(e -> {
            if (minTemp.getValue() > maxTemp.getValue())
                minTemp.setValue(maxTemp.getValue());
        });
        addValueSlider(temperaturePanel, "Year Length", 1, 3000, config.getYearLength(), (v) -> config.setYear(v, config.getTempVariation()));
        addValueSlider(temperaturePanel, "Season Variation", -200, 200, config.getTempVariation(), (v) -> config.setYear(config.getYearLength(), v));
    }

    private void addOrganismPanel() {
        JPanel organismPanel = new JPanel();
        LayoutManager panelLayout = new BoxLayout(organismPanel, BoxLayout.Y_AXIS);
        organismPanel.setLayout(panelLayout);
        addTab("Organism", new JScrollPane(organismPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER));
        JPanel seedPanel = addGroupPanel(organismPanel, "Seed Values");
        addValueSlider(seedPanel, "Count", 1, 1000,
                config.getSeedCount(), config::setSeedCount);
        addValueSlider(seedPanel, "Initial Energy", 100, 1000,
                config.getInitialEnergy(), config::setInitialEnergy);
        JPanel ratePanel = addGroupPanel(organismPanel, "Rates");
        addValueSlider(ratePanel, "Consumption Rate", 1, 100,
                config.getConsumptionRate(), config::setConsumptionRate);
        addValueSlider(ratePanel, "Base Cost", 0, 20,
                config.getBaseCost(), config::setBaseCost);
        addValueSlider(ratePanel, "Aging Cost", 0, 20,
                config.getAgingRate(), config::setAgingRate);
        addValueSlider(ratePanel, "Size Cost", 0, 20,
                config.getSizeRate(), config::setSizeRate);

        JPanel costPanel = addGroupPanel(organismPanel, "Costs");
        for (WorldActivity activity : WorldActivity.values()) {
            addValueSlider(costPanel, WorldActivity.describe(activity.ordinal()),
                    0, 10, config.getActivityCost(activity), v -> config.setActivityCost(activity, v));
        }
    }

    private JPanel addGroupPanel(JPanel container, String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        Border border = createTitledBorder(createEtchedBorder(), title);
        panel.setBorder(border);
        container.add(panel);
        layout.gridy = 0;
        return panel;
    }

    private JSlider addValueSlider(JPanel panel, String label, int min, int max, int initial, Consumer<Integer> setter) {
        layout.anchor = GridBagConstraints.WEST;
        layout.gridwidth = 1;
        layout.gridx = 0;
        panel.add(new JLabel(label), layout);
        layout.anchor = GridBagConstraints.EAST;
        layout.gridx = 1;
        JLabel value = new JLabel(String.valueOf(initial));
        panel.add(value, layout);
        layout.gridx = 0;
        layout.gridwidth = 2;
        layout.anchor = GridBagConstraints.WEST;
        layout.gridy++;
        JSlider slider = new JSlider(min, max, initial);
        panel.add(slider, layout);
        slider.addChangeListener(e -> setter.accept(slider.getValue()));
        slider.addChangeListener(e -> value.setText(String.valueOf(slider.getValue())));
        layout.gridy++;
        return slider;
    }
}
