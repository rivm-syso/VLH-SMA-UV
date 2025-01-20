package nl.rivm.uvsg.configuration;

import nl.rivm.uvsg.SensorReader;
import nl.rivm.uvsg.persistence.DataFileWriter;
import nl.rivm.uvsg.persistence.SensorReadings;
import nl.rivm.uvsg.sensor.Sensor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

//TODO MMo - see BeanFactoryIntegrationTest: 'prevent ...'
@SpringBootTest
//TODO MMo - remove the inline mock stuff
@TestPropertySource(properties = {"mock-sensor-explorer=true", "mock-readings=true"})
class BeanFactoryIntegrationTest {

    public static final String PQR = "PQR";
    public static final String XYZ = "XYZ";

    // As @SpringBootTest creates the application context and calls SensorReader.run().
    // As we need only the context, we prevent the run() by mocking the SensorReader
    @MockBean
    SensorReader sensorReader;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    void newSensor() {

        Sensor sensor1 = applicationContext.getBean(Sensor.class, (short)0, PQR);
        Sensor sensor2 = applicationContext.getBean(Sensor.class, (short)1, XYZ);
        assertFalse(sensor1 == sensor2);
        assertEquals(PQR, sensor1.getDeviceName());
        assertEquals((short) 0, sensor1.getSlaveId());
        assertEquals(XYZ, sensor2.getDeviceName());
        assertEquals((short) 1, sensor2.getSlaveId());
    }

    @Test
    void newDataFileWriter() {
        DataFileWriter dataFileWriter1 = applicationContext.getBean(
                DataFileWriter.class, "", new SensorReadings(null, null));
        DataFileWriter dataFileWriter2 = applicationContext.getBean(
                DataFileWriter.class, "", null);
        assertFalse(dataFileWriter1 == dataFileWriter2);
    }
}
