package com.udacity.catpoint.security.service;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import com.udacity.catpoint.image.service.IService;
import com.udacity.catpoint.security.application.StatusListener;
import com.udacity.catpoint.security.data.AlarmStatus;
import static com.udacity.catpoint.security.data.AlarmStatus.ALARM;
import static com.udacity.catpoint.security.data.AlarmStatus.NO_ALARM;
import static com.udacity.catpoint.security.data.AlarmStatus.PENDING_ALARM;
import com.udacity.catpoint.security.data.ArmingStatus;
import com.udacity.catpoint.security.data.SecurityRepository;
import com.udacity.catpoint.security.data.Sensor;

/**
 * Service that receives information about changes to the security system. Responsible for
 * forwarding updates to the repository and making any decisions about changing the system state.
 * <p>
 * This is the class that should contain most of the business logic for our system, and it is the
 * class you will be writing unit tests for.
 */
public class SecurityService {

    private final IService imageService;
    private final SecurityRepository securityRepository;
    private final Set<StatusListener> statusListeners = new HashSet<>();
    private Boolean catDetection = false;

    public SecurityService(SecurityRepository securityRepository, IService imageService) {
        this.securityRepository = securityRepository;
        this.imageService = imageService;
    }

    /**
     * Sets the current arming status for the system. Changing the arming status
     * may update both the alarm status.
     *
     * @param armingStatus
     */
    public void setArmingStatus(ArmingStatus armingStatus) {
        if (catDetection && armingStatus == ArmingStatus.ARMED_HOME) {
            setAlarmStatus(ALARM);
        }
        if (armingStatus == ArmingStatus.DISARMED) {
            setAlarmStatus(NO_ALARM);
        } else {
            ConcurrentSkipListSet<Sensor> sensors = new ConcurrentSkipListSet<>(getSensors());
            sensors.forEach(sensor -> changeSensorActivationStatus(sensor, false));
        }

        securityRepository.setArmingStatus(armingStatus);
        statusListeners.forEach(StatusListener::sensorStatusChanged);
    }

    private boolean getAllSensorsFromState(boolean state) {
        return getSensors().stream().allMatch(sensor -> sensor.getActive() == state);
    }

    /**
     * Internal method that handles alarm status changes based on whether
     * the camera currently shows a cat.
     *
     * @param cat True if a cat is detected, otherwise false.
     */
    private void catDetected(Boolean cat) {
        catDetection = cat;

        if (cat && getArmingStatus() == ArmingStatus.ARMED_HOME) {
            setAlarmStatus(ALARM);
        } else if (!cat && getAllSensorsFromState(false)) {
            setAlarmStatus(NO_ALARM);
        }
        statusListeners.forEach(sl -> sl.catDetected(cat));
    }

    /**
     * Register the StatusListener for alarm system updates from within the SecurityService.
     *
     * @param statusListener
     */
    public void addStatusListener(StatusListener statusListener) {
        statusListeners.add(statusListener);
    }

    public void removeStatusListener(StatusListener statusListener) {
        statusListeners.remove(statusListener);
    }

    /**
     * Change the alarm status of the system and notify all listeners.
     *
     * @param status
     */
    public void setAlarmStatus(AlarmStatus status) {
        securityRepository.setAlarmStatus(status);
        statusListeners.forEach(sl -> sl.notify(status));
    }

    /**
     * Internal method for updating the alarm status when a sensor has been activated.
     */
    private void handleSensorActivated() {
        if (securityRepository.getArmingStatus() == ArmingStatus.DISARMED) {
            return; //no problem if the system is disarmed
        }
        switch (securityRepository.getAlarmStatus()) {
            case NO_ALARM -> setAlarmStatus(PENDING_ALARM);
            default -> setAlarmStatus(ALARM); // there is no case ALARM because is must be different before coming to this method
        }
    }

    /**
     * Internal method for updating the alarm status when a sensor has been deactivated
     */
    private void handleSensorDeactivated() {
        switch (securityRepository.getAlarmStatus()) {
            case PENDING_ALARM -> setAlarmStatus(NO_ALARM);
            default -> setAlarmStatus(ALARM);
        }
    }

    /**
     * Change the activation status for the specified sensor and update alarm
     * status if necessary.
     *
     * @param sensor
     * @param active
     */
    public void changeSensorActivationStatus(Sensor sensor, Boolean active) {
        AlarmStatus actualAlarmStatus = securityRepository.getAlarmStatus();
        ArmingStatus armingStatus = securityRepository.getArmingStatus();

        // If the system is not in ALARM state
        if (actualAlarmStatus != AlarmStatus.ALARM) {
            // If the sensor is being activated
            if (active) {
                handleSensorActivated();
            } // If the sensor is being deactivated and it was active
            else if (sensor.getActive()) {
                handleSensorDeactivated();
            }
        }

        // Update the sensor's active status
        sensor.setActive(active);
        securityRepository.updateSensor(sensor);

        // Check if the system is armed and if all sensors are inactive
        if (armingStatus == ArmingStatus.ARMED_HOME || armingStatus == ArmingStatus.ARMED_AWAY) {
            // Check if all sensors are inactive
            boolean allSensorsInactive = securityRepository.getSensors().stream().allMatch(s -> !s.getActive());

            // If all sensors are inactive, set the alarm status to NO_ALARM
            if (allSensorsInactive) {
                securityRepository.setAlarmStatus(AlarmStatus.NO_ALARM);
            }
        }
    }

    /**
     * Send an image to the SecurityService for processing. The securityService will use its provided
     * ImageService to analyze the image for cats and update the alarm status accordingly.
     *
     * @param currentCameraImage
     */
    public void processImage(BufferedImage currentCameraImage) {
        catDetected(imageService.imageContainsCat(currentCameraImage, 50.0f));
    }

    public AlarmStatus getAlarmStatus() {
        return securityRepository.getAlarmStatus();
    }

    public Set<Sensor> getSensors() {
        return securityRepository.getSensors();
    }

    public void addSensor(Sensor sensor) {
        securityRepository.addSensor(sensor);
    }

    public void removeSensor(Sensor sensor) {
        securityRepository.removeSensor(sensor);
    }

    public ArmingStatus getArmingStatus() {
        return securityRepository.getArmingStatus();
    }
}
