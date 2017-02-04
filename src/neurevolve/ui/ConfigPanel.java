package neurevolve.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;
import static javax.swing.BorderFactory.createEtchedBorder;
import static javax.swing.BorderFactory.createTitledBorder;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
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
        addWorldPanel(config);
        addOrganismPanel();
    }

    private void addWorldPanel(WorldConfiguration config1) {
        JPanel worldPanel = new JPanel();
        addTab("World", worldPanel);
        layout.gridy = 0;
        JPanel mutationPanel = addGroupPanel(worldPanel, "Mutation");
        addValueSlider(mutationPanel, "Rate", 1, 100, config1.getMutationRate(), config1::setMutationRate);

        JPanel temperaturePanel = addGroupPanel(worldPanel, "Temperature");
        JSlider minTemp = addValueSlider(temperaturePanel, "At Pole", -200, 400, config1.getMinTemp(), (v) -> config1.setTemperatureRange(v, config1.getMaxTemp()));
        JSlider maxTemp = addValueSlider(temperaturePanel, "At Equator", -200, 400, config1.getMaxTemp(), (v) -> config1.setTemperatureRange(config1.getMinTemp(), v));
        minTemp.addChangeListener(e -> {
            if (minTemp.getValue() > maxTemp.getValue())
                maxTemp.setValue(minTemp.getValue());
        });
        maxTemp.addChangeListener(e -> {
            if (minTemp.getValue() > maxTemp.getValue())
                minTemp.setValue(maxTemp.getValue());
        });
        addValueSlider(temperaturePanel, "Year Length", 1, 3000, config1.getYearLength(), (v) -> config1.setYear(v, config1.getTempVariation()));
        addValueSlider(temperaturePanel, "Season Variation", -200, 200, config1.getTempVariation(), (v) -> config1.setYear(config1.getYearLength(), v));
    }

    private void addOrganismPanel() {
        JPanel organismPanel = new JPanel();
        addTab("Organism", organismPanel);
        JPanel ratePanel = addGroupPanel(organismPanel, "Rates");
        addValueSlider(ratePanel, "Time Between Splits", 0, 20,
                config.getTimeBetweenSplits(), config::setTimeBetweenSplits);
        addValueSlider(ratePanel, "Consumption Rate", 1, 100,
                config.getConsumptionRate(), config::setConsumptionRate);
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
