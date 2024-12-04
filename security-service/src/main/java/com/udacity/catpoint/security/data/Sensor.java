package com.udacity.catpoint.security.data;

import java.util.Objects;
import java.util.UUID;

import com.google.common.collect.ComparisonChain;

/**
 * Sensor POJO (Plain Old Java Object) representing a sensor in the system.
 * The sensor has a unique ID, a name, a status indicating whether it's active or inactive,
 * and a type indicating the kind of sensor.
 * This class also implements Comparable to allow sorting by name, type, and ID.
 */
public class Sensor implements Comparable<Sensor> {

    private UUID sensorId;
    private String name;
    private boolean active;  // Using primitive boolean for performance
    private SensorType sensorType;

    /**
     * Constructor to create a new Sensor with a name and type. 
     * The sensor will be initialized as inactive.
     *
     * @param name       The name of the sensor
     * @param sensorType The type of the sensor (e.g., motion, door, etc.)
     */
    public Sensor(String name, SensorType sensorType) {
        this.name = name;
        this.sensorType = sensorType;
        this.sensorId = UUID.randomUUID();
        this.active = false;  // Default status is inactive
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;  // If comparing with the same object, return true
        if (o == null || getClass() != o.getClass()) return false;  // Null check and class type check
        Sensor sensor = (Sensor) o;
        return sensorId.equals(sensor.sensorId);  // Compare based on unique sensorId
    }

    @Override
    public int hashCode() {
        return Objects.hash(sensorId);  // Generate hashCode based on the unique sensorId
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public UUID getSensorId() {
        return sensorId;
    }

    public void setSensorId(UUID sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * Compares this sensor to another sensor based on name, type, and unique ID.
     * Sorting is performed in the following order: name, sensor type, then ID.
     */
    @Override
    public int compareTo(Sensor o) {
        return ComparisonChain.start()
                .compare(this.name, o.name)  // Compare by name
                .compare(this.sensorType.toString(), o.sensorType.toString())  // Compare by type
                .compare(this.sensorId, o.sensorId)  // Compare by unique ID
                .result();  // Return comparison result
    }
}
