package com.udacity.catpoint.security.data;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

/**
 * Fake repository implementation for demo purposes. Stores state information in local
 * memory and writes it to user preferences between app loads. This implementation is
 * intentionally a little hard to use in unit tests, so watch out!
 */
public class PretendDatabaseSecurityRepositoryImpl implements SecurityRepository {

    private Set<Sensor> sensors;
    private AlarmStatus alarmStatus;
    private ArmingStatus armingStatus;

    // Preference keys for storing data in user preferences
    private static final String SENSORS = "SENSORS";
    private static final String ALARM_STATUS = "ALARM_STATUS";
    private static final String ARMING_STATUS = "ARMING_STATUS";

    private static Preferences prefs = Preferences.userNodeForPackage(PretendDatabaseSecurityRepositoryImpl.class);
    private static Gson gson = new Gson(); // Used to serialize objects into JSON

    public PretendDatabaseSecurityRepositoryImpl() {
        // Load system state from prefs, or use default values
        loadPreferences();
    }

    /**
     * Helper method to save the current sensors set to preferences.
     */
    private void saveSensors() {
        prefs.put(SENSORS, gson.toJson(sensors));
    }

    /**
     * Helper method to save the current alarm status to preferences.
     */
    private void saveAlarmStatus() {
        prefs.put(ALARM_STATUS, alarmStatus.toString());
    }

    /**
     * Helper method to save the current arming status to preferences.
     */
    private void saveArmingStatus() {
        prefs.put(ARMING_STATUS, armingStatus.toString());
    }

    @Override
    public void addSensor(Sensor sensor) {
        sensors.add(sensor);
        saveSensors(); // Save the updated sensors set to preferences
    }

    @Override
    public void removeSensor(Sensor sensor) {
        sensors.remove(sensor);
        saveSensors(); // Save the updated sensors set to preferences
    }

    @Override
    public void updateSensor(Sensor sensor) {
        sensors.remove(sensor);  // Remove the old sensor if exists
        sensors.add(sensor);     // Add the updated sensor
        saveSensors();           // Save the updated sensors set to preferences
    }

    @Override
    public void setAlarmStatus(AlarmStatus alarmStatus) {
        this.alarmStatus = alarmStatus;
        saveAlarmStatus(); // Save the updated alarm status
    }

    @Override
    public void setArmingStatus(ArmingStatus armingStatus) {
        this.armingStatus = armingStatus;
        saveArmingStatus(); // Save the updated arming status
    }

    @Override
    public Set<Sensor> getSensors() {
        return sensors;
    }

    @Override
    public AlarmStatus getAlarmStatus() {
        return alarmStatus;
    }

    @Override
    public ArmingStatus getArmingStatus() {
        return armingStatus;
    }
    
    /**
     * Loads the system state from preferences. Defaults if no value is present.
     */
    @Override
    public void loadPreferences() {
        // Load alarm and arming status
        alarmStatus = AlarmStatus.valueOf(prefs.get(ALARM_STATUS, AlarmStatus.NO_ALARM.toString()));
        armingStatus = ArmingStatus.valueOf(prefs.get(ARMING_STATUS, ArmingStatus.DISARMED.toString()));

        // Load sensor list from preferences, or initialize an empty set
        String sensorString = prefs.get(SENSORS, null);
        if (sensorString == null) {
            sensors = new TreeSet<>();
        } else {
            Type type = new TypeToken<Set<Sensor>>() {}.getType();
            sensors = gson.fromJson(sensorString, type);
        }
    }
}
