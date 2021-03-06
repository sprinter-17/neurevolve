package neurevolve.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.Optional;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import neurevolve.world.Configuration;
import neurevolve.world.Configuration.Value;
import static neurevolve.world.Configuration.Value.*;
import neurevolve.world.GroundElement;
import neurevolve.world.WorldActivity;

public class ConfigPanel extends JTabbedPane {

    private final Configuration config;
    private final GridBagConstraints layout = new GridBagConstraints();

    public ConfigPanel(Configuration config) {
        super();
        this.config = config;
        layout.insets = new Insets(0, 8, 0, 8);
        addWorldPanel();
        addGroundPanel();
        addOrganismPanel();
        addActivityPanel();
    }

    private void addWorldPanel() {
        JPanel worldPanel = new JPanel();
        LayoutManager panelLayout = new BoxLayout(worldPanel, BoxLayout.Y_AXIS);
        worldPanel.setLayout(panelLayout);
        addTab("World", new JScrollPane(worldPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER));
        layout.gridy = 0;
        JPanel mutationPanel = addGroupPanel(worldPanel, "Mutation Rate");
        new ConfigValueSlider("Normal", NORMAL_MUTATION_RATE).addTo(mutationPanel);
        new ConfigValueSlider("Radiated", RADIATION_MUTATION_RATE).addTo(mutationPanel);

        JPanel temperaturePanel = addGroupPanel(worldPanel, "Temperature");
        ConfigValueSlider minTemp = new ConfigValueSlider("At Pole", MIN_TEMP);
        minTemp.addTo(temperaturePanel);
        ConfigValueSlider maxTemp = new ConfigValueSlider("At Equator", MAX_TEMP);
        maxTemp.addTo(temperaturePanel);
        minTemp.setLessThan(maxTemp);
        new ConfigValueSlider("Year Length", YEAR_LENGTH).addTo(temperaturePanel);
        new ConfigValueSlider("Season Variation", TEMP_VARIATION).addTo(temperaturePanel);
    }

    private void addGroundPanel() {
        JPanel groundPanel = new JPanel();
        groundPanel.setLayout(new BoxLayout(groundPanel, BoxLayout.Y_AXIS));
        addTab("Ground", new JScrollPane(groundPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER));
        JPanel effectsPanel = addGroupPanel(groundPanel, "Effects");
        new ConfigValueSlider("Acid Toxicity", ACID_TOXICITY).addTo(effectsPanel);

        JPanel halfLife = addGroupPanel(groundPanel, "Half Lives");
        for (GroundElement element : GroundElement.values()) {
            Consumer<Integer> setter = v -> config.setHalfLife(element, v);
            ConfigValueSlider valueSlider = new ConfigValueSlider(element.getName(),
                    HALF_LIFE.getMin(),
                    config.getHalfLife(element),
                    HALF_LIFE.getMax(),
                    setter);
            valueSlider.withMaxValue("None").addTo(halfLife);
        }
        layout.gridy = 0;
    }

    private void addOrganismPanel() {
        JPanel organismPanel = new JPanel();
        LayoutManager panelLayout = new BoxLayout(organismPanel, BoxLayout.Y_AXIS);
        organismPanel.setLayout(panelLayout);
        addTab("Organism", new JScrollPane(organismPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER));
        JPanel seedPanel = addGroupPanel(organismPanel, "Seed Values");
        new ConfigValueSlider("Count", SEED_COUNT).addTo(seedPanel);
        new ConfigValueSlider("Initial Energy", INITIAL_ENERGY).addTo(seedPanel);

        JPanel splitPanel = addGroupPanel(organismPanel, "Split Limits");
        new ConfigValueSlider("Minimum Split Time", MIN_SPLIT_TIME).addTo(splitPanel);
        new ConfigValueSlider("Minimum Split Energy", MIN_SPLIT_ENERGY).addTo(splitPanel);

        JPanel energyPanel = addGroupPanel(organismPanel, "Energy");
        new ConfigValueSlider("Consumption Rate", CONSUMPTION_RATE).addTo(energyPanel);
        new ConfigValueSlider("Maximum Energy", MAX_ENERGY).addTo(energyPanel);

        JPanel costPanel = addGroupPanel(organismPanel, "Costs");
        new ConfigValueSlider("Base Cost", BASE_COST).addTo(costPanel);
        new ConfigValueSlider("Aging Cost", Value.AGING_RATE).addTo(costPanel);
        new ConfigValueSlider("Size Cost", Value.SIZE_RATE).addTo(costPanel);
    }

    private void addActivityPanel() {
        JPanel activityPanel = new JPanel();
        activityPanel.setLayout(new BoxLayout(activityPanel, BoxLayout.Y_AXIS));
        addTab("Activities", new JScrollPane(activityPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER));

        JPanel costPanel = addGroupPanel(activityPanel, "Costs");
        for (WorldActivity activity : WorldActivity.values()) {
            addValueSlider(costPanel, WorldActivity.describe(activity.ordinal()),
                    0, 50, config.getActivityCost(activity), v -> config.setActivityCost(activity, v));
        }

        JPanel factorPanel = addGroupPanel(activityPanel, "Repeat Factor");
        for (WorldActivity activity : WorldActivity.values()) {
            addValueSlider(factorPanel, WorldActivity.describe(activity.ordinal()),
                    0, 200, config.getActivityFactor(activity), v -> config.setActivityFactor(activity, v));
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

    private class ConfigValueSlider implements ChangeListener {

        private final JLabel label;
        private final JLabel value;
        private final JSlider slider;
        private final int min;
        private final int max;
        private final Consumer<Integer> setter;
        private Optional<String> maxValue = Optional.empty();

        public ConfigValueSlider(String name, int min, int initial, int max, Consumer<Integer> setter) {
            this.min = min;
            this.max = max;
            this.setter = setter;
            label = new JLabel(name);
            value = new JLabel(toString(initial));
            slider = new JSlider(min, max, initial);
            slider.addChangeListener(this);
        }

        public ConfigValueSlider(String name, Value key) {
            this(name, key.getMin(), config.getValue(key), key.getMax(), v -> config.setValue(key, v));
        }

        public ConfigValueSlider withMaxValue(String text) {
            maxValue = Optional.of(text);
            value.setText(toString(slider.getValue()));
            return this;
        }

        public void setLessThan(ConfigValueSlider bigger) {
            slider.addChangeListener(e -> {
                if (slider.getValue() > bigger.slider.getValue())
                    bigger.slider.setValue(slider.getValue());
            });
            bigger.slider.addChangeListener(e -> {
                if (slider.getValue() > bigger.slider.getValue())
                    slider.setValue(bigger.slider.getValue());
            });
        }

        public void addTo(JPanel panel) {
            layout.anchor = GridBagConstraints.WEST;
            layout.gridwidth = 1;
            layout.gridx = 0;
            panel.add(label, layout);
            layout.anchor = GridBagConstraints.EAST;
            layout.gridx = 1;
            panel.add(value, layout);
            layout.gridx = 0;
            layout.gridwidth = 2;
            layout.anchor = GridBagConstraints.WEST;
            layout.gridy++;
            panel.add(slider, layout);
            layout.gridy++;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            setter.accept(slider.getValue());
            value.setText(toString(slider.getValue()));
        }

        private String toString(int val) {
            if (val == max && maxValue.isPresent())
                return maxValue.get();
            else
                return String.valueOf(val);
        }

    }

    private JSlider addValueSlider(JPanel panel, String label,
            int min, int max, int initial, Consumer<Integer> setter) {
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
        if (max - min > 1000) {
            slider.setMajorTickSpacing(50);
            slider.setPaintTicks(true);
            slider.setSnapToTicks(true);
        }
        panel.add(slider, layout);
        slider.addChangeListener(e -> setter.accept(slider.getValue()));
        slider.addChangeListener(e -> value.setText(String.valueOf(slider.getValue())));
        layout.gridy++;
        return slider;
    }
}
