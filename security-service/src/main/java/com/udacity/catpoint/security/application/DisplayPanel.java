package com.udacity.catpoint.security.application;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.udacity.catpoint.security.data.AlarmStatus;
import com.udacity.catpoint.security.service.SecurityService;
import com.udacity.catpoint.security.service.StyleService;

import net.miginfocom.swing.MigLayout;

/**
 * Displays the current status of the system. Implements the StatusListener
 * interface so that it can be notified whenever the status changes.
 */
public class DisplayPanel extends JPanel implements StatusListener {

    // ---------------------- Instance Variables ----------------------

    private final JLabel currentStatusLabel; // Label to display the current alarm status

    public DisplayPanel(SecurityService securityService) {
        super();
        setLayout(new MigLayout());

        // Register this panel as a listener to changes in system status
        securityService.addStatusListener(this);

        // Initialize UI components
        JLabel panelLabel = new JLabel("Very Secure Home Security");
        JLabel systemStatusLabel = new JLabel("System Status:");
        currentStatusLabel = new JLabel(); // Label to show the current status of the alarm system

        // Set the font for the heading
        panelLabel.setFont(StyleService.HEADING_FONT);

        // Initial call to set the alarm status in the label
        notify(securityService.getAlarmStatus());

        // Add components to the panel with layout constraints
        add(panelLabel, "span 2, wrap");
        add(systemStatusLabel);
        add(currentStatusLabel, "wrap");
    }

    /**
     * Called when the alarm status changes. Updates the display of the system's status.
     * 
     * @param status The new alarm status.
     */
    @Override
    public void notify(AlarmStatus status) {
        currentStatusLabel.setText(status.getDescription());  // Set the text to the description of the status
        currentStatusLabel.setBackground(status.getColor()); // Set the background color based on the status
        currentStatusLabel.setOpaque(true);  // Make the background color visible
    }

    @Override
    public void catDetected(boolean catDetected) {
        // No behavior necessary for this panel when a cat is detected
    }

    @Override
    public void sensorStatusChanged() {
        // No behavior necessary for this panel when sensor status changes
    }
}
