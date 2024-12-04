package com.udacity.catpoint.security.data;

import java.awt.Color;

/**
 * List of potential states the security system can use to describe how the system is armed.
 * Also contains metadata about what text and color is associated with the arming status.
 */
public enum ArmingStatus {
    DISARMED("Disarmed", new Color(120, 200, 30)), // System is disarmed
    ARMED_HOME("Armed - At Home", new Color(190, 180, 50)), // System is armed for home
    ARMED_AWAY("Armed - Away", new Color(170, 30, 150)); // System is armed for away

    private final String description;
    private final Color color;

    /**
     * Constructor for the ArmingStatus enum.
     *
     * @param description The text description of the arming status.
     * @param color The color associated with the arming status.
     */
    ArmingStatus(String description, Color color) {
        this.description = description;
        this.color = color;
    }

    /**
     * Get the description of the arming status.
     *
     * @return The description of the arming status.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the color associated with the arming status.
     *
     * @return The color of the arming status.
     */
    public Color getColor() {
        return color;
    }
}
