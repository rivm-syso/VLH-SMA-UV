package nl.rivm.uvsg.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class SensorReadingsTest {

    private SensorReadings sensorReadings;
    private ZonedDateTime now;

    @BeforeEach
    void setUp() {
        now = ZonedDateTime.now();
        sensorReadings = new SensorReadings(now, null);
    }

    @Test
    void testAddReadings() {
        int nrOfReadings = 1000_000;
        for(int i = 0; i < nrOfReadings; ) {
            sensorReadings.addReading(++i);
        }
        sensorReadings = sensorReadings.toImmutable();
        assertEquals(now, sensorReadings.getUtcDateTime());
        assertEquals(nrOfReadings, sensorReadings.getSize());
        assertEquals(500_000.5, sensorReadings.getAverage());
    }

    @Test
    void testAverageAndStandardDeviation() {
        sensorReadings.addReading(10);
        sensorReadings.addReading(0);
        sensorReadings.addReading(0);
        sensorReadings.addReading(-10);
        sensorReadings.toImmutable();
        assertEquals(0, sensorReadings.getAverage());
        assertEquals(Math.sqrt(50.0), sensorReadings.getStandardDeviation());
    }

    @Test
    void testToImmutable() {
        sensorReadings.addReading(1);
        sensorReadings.addReading(2);
        assertThrowsExactly(IllegalStateException.class, () -> sensorReadings.getSize());
        assertThrowsExactly(IllegalStateException.class, () -> sensorReadings.getAverage());
        assertThrowsExactly(IllegalStateException.class, () -> sensorReadings.getStandardDeviation());
        sensorReadings = sensorReadings.toImmutable();
        assertThrowsExactly(IllegalStateException.class, () -> sensorReadings.addReading(3));
    }
}
