package com.udacity.catpoint.security;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.udacity.catpoint.image.service.IService;
import com.udacity.catpoint.security.data.AlarmStatus;
import com.udacity.catpoint.security.data.ArmingStatus;
import com.udacity.catpoint.security.data.SecurityRepository;
import com.udacity.catpoint.security.data.Sensor;
import com.udacity.catpoint.security.data.SensorType;
import com.udacity.catpoint.security.service.SecurityService;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    private SecurityService securityService;
    private Sensor testSensor;
    private Set<Sensor> testSensors;
    private final String sensorId = UUID.randomUUID().toString();

    @Mock
    private IService imageService;

    @Mock
    private SecurityRepository securityRepository;

    @BeforeEach
    void setup() {
        securityService = new SecurityService(securityRepository, imageService);
        //1 sensor
        testSensor = new Sensor(sensorId, SensorType.DOOR);
        testSensor.setActive(true);

        // 3 sensors : 2 active, 1 inactive
        testSensors = new HashSet<>();
        Sensor sensor1 = new Sensor(sensorId, SensorType.DOOR);
        sensor1.setActive(true);

        Sensor sensor2 = new Sensor(sensorId, SensorType.DOOR);
        sensor2.setActive(false);

        Sensor sensor3 = new Sensor(sensorId, SensorType.DOOR);
        sensor3.setActive(true);

        testSensors.add(sensor1);
        testSensors.add(sensor2);
        testSensors.add(sensor3);
    }

    //case 1: arming status != ArmingStatus.DISARMED & 1 sensor active -> alarmStatus = pending alarm
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY", "ARMED_HOME"})
    void alarm_is_armed_and_a_sensor_activated_should_system_is_pending_alarm_statatus(ArmingStatus armingStatus) {
        // sensor is activate
        // to get the case PENDING_ALARM in HandleSensorActivated().
        // arming status must != ArmingStatus.DISARMED  & alarm status must be NO_ALARM.

        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(testSensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    //case 2: arming status != ArmingStatus.DISARMED & 1 sensor active & alarmStatus = pending alarm => alarmStatus = alarm
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY", "ARMED_HOME"})
    void alarm_is_armed_and_a_sensor_activated_and_system_is_pending_alarm_should_set_alarm_state_to_alarm(ArmingStatus armingStatus) {
        // sensor is activate
        // to get the case default: setAlarmStatus(ALARM); in HandleSensorActivated().
        // arming status must != ArmingStatus.DISARMED & AlarmStatus must != NO_ALARM (already PENDING_ALARM)
        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(testSensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    //case 3: alarm status is pending and ALL sensor is inactive -> alarmStatus = NO ALARM
    @Test
    void pending_alarm_and_sensor_deactivated_no_alarm_should_trigger() {
        // sensor is activate but we change it to deactive => handleSensorDeactivated
        // to get the case setAlarmStatus(NO_ALARM), the alarm status must be PENDING_ALARM (already have)
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(testSensor, false);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    //case 4: alrm status is active (already), sensor is inactive or not (true or false) -> alarmStatus must not be change.
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void alarm_active_and_sensor_status_changed_should_no_change_in_alarm_state(boolean testSensorActiveStatus) {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(testSensor, testSensorActiveStatus);
        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    //case 5: one sensor is active & this sensor is active one again & alarm status is pending => set alarm status = alarm
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY", "ARMED_HOME"})
    void sensor_is_active_and_it_activated_again_and_system_is_pending_alarm_should_set_alarm_state_to_alarm(ArmingStatus armingStatus) {
        // sensor is activate
        // to get the case default: setAlarmStatus(ALARM); in HandleSensorActivated().
        // arming status must != ArmingStatus.DISARMED & AlarmStatus must != NO_ALARM (already PENDING_ALARM)
        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(testSensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    //case 6: a sensor is already inactive (false) & it inactive again (false) -> alarmStatus (ALARM) must not be change.
    @Test
    void alarm_active_and_sensor_status_changed_should_no_change_in_alarm_state() {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM); // set Alarm status to ALARM as they say

        testSensor.setActive(false); // already inactive
        securityService.changeSensorActivationStatus(testSensor, false); // inactive again

        //it never come to handleSensorDeactivated - where the setAlarmStatus(status) 
        //because the alarm status must not be ALARM to come here
        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    //case 7: cat is detected & arming status is armed home -> alarmStatus set to ALARM
    @Test
    void cat_detected_and_arming_status_armed_home_shuold_alarm_status_set_to_alarm() {
        //cat detected
        BufferedImage mockImage = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        // arming status is armed home
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        securityService.processImage(mockImage);

        //to set AlarmStatus to Alarm in . (cat && getArmingStatus() == ArmingStatus.ARMED_HOME) must be true
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    //case 8: cat not detected & all sensor is inactive -> set alarm status to No ALARM
    @Test
    void cat_not_detected_and_all_sensors_inactive_should_set_alarm_status_to_no_alarm() {
        // all sensor is inactive
        testSensors.forEach(sensor -> sensor.setActive(false));
        when(securityRepository.getSensors()).thenReturn(testSensors);

        // cat not detected
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);

        securityService.processImage(mock(BufferedImage.class));

        //if (!cat && getAllSensorsFromState(false)) => set NO ALARM
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    //case 9: arming status is disarmed -> set alarmStatus = NO ALARM
    @Test
    void arming_status_is_disarmed_alarm_should_set_to_no_alarm() {
        securityService.setArmingStatus(ArmingStatus.DISARMED); // 
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    //case 10.1: arming status is armed -> set all sensor to inactive
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY", "ARMED_HOME"})
    void system_armed_all_sensors_should_be_deactivated(ArmingStatus armingStatus) {
        // all sensor are active
        testSensors.forEach(sensor -> sensor.setActive(true));
        when(securityRepository.getSensors()).thenReturn(testSensors);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);

        // first: there must not (detected a cat && armingStatus = ARMED_HOME)
        // second: armingStatus must not DISARMED
        // third: AlarmStatus must not ALARM (NO_ALARM for test 10.1)
        // then setArmingStatus -> changeSensorActivationStatus with activation status is set to false
        securityService.setArmingStatus(armingStatus);

        // then every sensors will be set inactive
        securityService.getSensors().forEach(sensor -> assertFalse(sensor.getActive()));
    }

    //case 10.2: arming status is armed -> set all sensor to inactive
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY", "ARMED_HOME"})
    void system_armed_all_sensors_should_be_deactivated_2(ArmingStatus armingStatus) {
        // 2 sensor active, 1 is inactive
        when(securityRepository.getSensors()).thenReturn(testSensors);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        // first: there must not (detected a cat && armingStatus = ARMED_HOME)
        // second: armingStatus must not DISARMED
        // third: AlarmStatus must not ALARM (PENDING_ALARM for test 10.2)
        // then setArmingStatus -> changeSensorActivationStatus with activation status is set to false
        securityService.setArmingStatus(armingStatus);

        // then every sensors will be set inactive
        securityService.getSensors().forEach(sensor -> assertFalse(sensor.getActive()));
    }

    // case 11 cat detected is first (because the world "while"), 
    // then we change arming status to armed home -> alarmStatus should be set to ALARM
    @Test
    void arming_status_is_change_to_armed_home_and_cat_detected_should_set_alarm_status_to_alarm() {
        // first: cat detected
        BufferedImage mockImage = new BufferedImage(123, 456, BufferedImage.TYPE_INT_RGB);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        securityService.processImage(mockImage);

        // second: change arming status to armed home
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        // finally: there (detected a cat && armingStatus = ARMED_HOME)
        // => set alarmStatus = alarm
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    // just to fill test coverage in securityService
    //case 12: one sensor is active & this sensor is active one again & arming status is disarmed => not setArmingStatus
    @Test
    void sensor_is_active_and_it_activated_again_and_system_is_disarmed_should_alarm_state_not_to_change() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        securityService.changeSensorActivationStatus(testSensor, true);

        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }
}
