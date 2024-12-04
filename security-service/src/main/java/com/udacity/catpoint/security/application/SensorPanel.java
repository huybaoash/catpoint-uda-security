package com.udacity.catpoint.security.application;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.udacity.catpoint.security.data.AlarmStatus;
import com.udacity.catpoint.security.data.Sensor;
import com.udacity.catpoint.security.data.SensorType;
import com.udacity.catpoint.security.service.SecurityService;
import com.udacity.catpoint.security.service.StyleService;

import net.miginfocom.swing.MigLayout;

/**
 * Panel that allows users to add sensors to their system. Sensors may be
 * manually set to "active" and "inactive" to test the system.
 */
public class SensorPanel extends JPanel implements StatusListener {

    private final SecurityService securityService;

    private JLabel panelLabel;
    private JLabel sensorNameLabel;
    private JLabel sensorTypeLabel;
    private JTextField sensorNameField;
    private JComboBox<SensorType> sensorTypeDropdown;
    private JButton addSensorButton;

    private JPanel sensorListPanel;
    private JPanel addSensorPanel;

    public SensorPanel(SecurityService securityService) {
        super();
        this.securityService = securityService;
        setLayout(new MigLayout());

        initializeComponents();
        configureLayout();
    }

    /**
     * Initializes the UI components.
     */
    private void initializeComponents() {
        panelLabel = new JLabel("Sensor Management");
        panelLabel.setFont(StyleService.HEADING_FONT);

        sensorNameLabel = new JLabel("Name:");
        sensorTypeLabel = new JLabel("Sensor Type:");

        sensorNameField = new JTextField();
        sensorTypeDropdown = new JComboBox<>(SensorType.values());

        addSensorButton = new JButton("Add New Sensor");
        addSensorButton.addActionListener(e -> addSensor(createNewSensor()));

        sensorListPanel = new JPanel();
        sensorListPanel.setLayout(new MigLayout());

        addSensorPanel = buildAddSensorPanel();
        updateSensorList(sensorListPanel);
    }

    /**
     * Configures the layout by adding components to the panel.
     */
    private void configureLayout() {
        add(panelLabel, "wrap");
        add(addSensorPanel, "span");
        add(sensorListPanel, "span");
    }

    /**
     * Builds the panel with the form for adding a new sensor.
     */
    private JPanel buildAddSensorPanel() {
        JPanel addSensorPanel = new JPanel();
        addSensorPanel.setLayout(new MigLayout());

        addSensorPanel.add(sensorNameLabel);
        addSensorPanel.add(sensorNameField, "width 50:100:200");
        addSensorPanel.add(sensorTypeLabel);
        addSensorPanel.add(sensorTypeDropdown, "wrap");
        addSensorPanel.add(addSensorButton, "span 3");

        return addSensorPanel;
    }

    /**
     * Creates a new sensor based on the user's input.
     */
    private Sensor createNewSensor() {
        String name = sensorNameField.getText();
        SensorType type = SensorType.valueOf(sensorTypeDropdown.getSelectedItem().toString());
        return new Sensor(name, type);
    }

    /**
     * Requests the current list of sensors and updates the provided panel to display them.
     * Sensors will display in the order that they are created.
     *
     * @param panel The Panel to populate with the current list of sensors.
     */
    private void updateSensorList(JPanel panel) {
        panel.removeAll();
        securityService.getSensors().stream().sorted().forEach(sensor -> {
            JLabel sensorLabel = createSensorLabel(sensor);
            JButton toggleButton = createToggleButton(sensor);
            JButton removeButton = createRemoveButton(sensor);

            panel.add(sensorLabel, "width 300:300:300");
            panel.add(toggleButton, "width 100:100:100");
            panel.add(removeButton, "wrap");
        });

        repaint();
        revalidate();
    }

    /**
     * Creates a label displaying the sensor's information.
     *
     * @param sensor The sensor to display.
     * @return A JLabel with the sensor's information.
     */
    private JLabel createSensorLabel(Sensor sensor) {
        return new JLabel(String.format("%s (%s): %s", 
            sensor.getName(), 
            sensor.getSensorType(), 
            sensor.getActive() ? "Active" : "Inactive"));
    }

    /**
     * Creates a button that toggles the sensor's activation status.
     *
     * @param sensor The sensor to toggle.
     * @return A JButton for toggling the sensor's activation.
     */
    private JButton createToggleButton(Sensor sensor) {
        JButton toggleButton = new JButton(sensor.getActive() ? "Deactivate" : "Activate");
        toggleButton.addActionListener(e -> setSensorActivity(sensor, !sensor.getActive()));
        return toggleButton;
    }

    /**
     * Creates a button that removes the sensor from the system.
     *
     * @param sensor The sensor to remove.
     * @return A JButton for removing the sensor.
     */
    private JButton createRemoveButton(Sensor sensor) {
        JButton removeButton = new JButton("Remove Sensor");
        removeButton.addActionListener(e -> removeSensor(sensor));
        return removeButton;
    }

    /**
     * Asks the securityService to change a sensor activation status and then rebuilds the current sensor list.
     *
     * @param sensor   The sensor to update.
     * @param isActive The sensor's activation status.
     */
    private void setSensorActivity(Sensor sensor, boolean isActive) {
        securityService.changeSensorActivationStatus(sensor, isActive);
        updateSensorList(sensorListPanel);
    }

    /**
     * Adds a sensor to the securityService and then rebuilds the sensor list.
     *
     * @param sensor The sensor to add.
     */
    private void addSensor(Sensor sensor) {
        if (securityService.getSensors().size() < 4) {
            securityService.addSensor(sensor);
            updateSensorList(sensorListPanel);
        } else {
            JOptionPane.showMessageDialog(null, 
                "To add more than 4 sensors, please subscribe to our Premium Membership!");
        }
    }

    /**
     * Removes a sensor from the securityService and then rebuilds the sensor list.
     *
     * @param sensor The sensor to remove.
     */
    private void removeSensor(Sensor sensor) {
        securityService.removeSensor(sensor);
        updateSensorList(sensorListPanel);
    }

    @Override
    public void notify(AlarmStatus status) {
        // No behavior necessary
    }

    @Override
    public void catDetected(boolean catDetected) {
        // No behavior necessary
    }

    @Override
    public void sensorStatusChanged() {
        updateSensorList(sensorListPanel);
    }
}
