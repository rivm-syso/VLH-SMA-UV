package nl.rivm.uvsg.configuration;

import nl.rivm.uvsg.persistence.DataFileWriter;
import nl.rivm.uvsg.persistence.SensorReadings;
import nl.rivm.uvsg.sensor.Sensor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.time.Clock;

@Configuration
public class BeanFactory {

    @Bean
    @Scope("prototype")
    public Sensor newSensor(short slaveId, String deviceName) {
        return new Sensor(slaveId, deviceName);
    }

    @Bean
    @Scope("prototype")
    public DataFileWriter newDataFileWriter(String sensorDeviceName, SensorReadings sensorReadings) {
        return new DataFileWriter(sensorDeviceName, sensorReadings);
    }

    /**
     * TODO when running tests with MockedClockFactory we need @ConditionalOnMissingBean
     * to prevent a BeanDefinitionOverrideException
     */
    @ConditionalOnMissingBean
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
