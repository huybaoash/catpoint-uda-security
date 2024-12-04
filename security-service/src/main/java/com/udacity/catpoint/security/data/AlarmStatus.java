package com.udacity.catpoint.security.data;

import java.awt.Color;

/**
 * List of potential states the alarm can have. Also contains metadata about what
 * text and color is associated with the alarm.
 */
public enum AlarmStatus {
    NO_ALARM("Cool and Good", new Color(120, 200, 30)),
    PENDING_ALARM("I'm in Danger...", new Color(200, 150, 20)),
    ALARM("Awooga!", new Color(250, 80, 50));

    private final String description;
    private final Color color;

    /**
     * Constructor for the AlarmStatus enum.
     *
     * @param description The text description of the alarm status.
     * @param color The color associated with the alarm status.
     */
    AlarmStatus(String description, Color color) {
        this.description = description;
        this.color = color;
    }

    /**
     * Get the description of the alarm status.
     *
     * @return The description of the alarm status.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the color associated with the alarm status.
     *
     * @return The color of the alarm status.
     */
    public Color getColor() {
        return color;
    }
}
