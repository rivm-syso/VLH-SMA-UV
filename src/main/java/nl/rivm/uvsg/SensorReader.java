package nl.rivm.uvsg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.rivm.uvsg.configuration.Properties;
import nl.rivm.uvsg.persistence.DataFileWriter;
import nl.rivm.uvsg.sensor.Sensor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class SensorReader implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final Properties properties;
    private final SensorExplorer sensorExplorer;

    private final Clock clock;

    // TODO MMo: dit moet tzt verdwijnen als de service netjes gemocked wordt vanuit de IntgrTest
    private static final short SLAVE_ID_6 = 6;
    private static final short SLAVE_ID_1 = 1;
    static int[] responseSequenceTest = {-1, 2, 5, 6, 5, 7};
    static int[] currIdx = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    public void run(String... args) {

        try {
            List<Sensor> sensors = getSensors();
            if(sensors.isEmpty()) {
                short slaveIdRangeEnd = SensorExplorer.SLAVE_ID_RANGE_START + SensorExplorer.MAXIMUM_NUMBER_OF_SLAVES;
                log.error("No devices were found in the Modbus address range {} to {}",
                        SensorExplorer.SLAVE_ID_RANGE_START, slaveIdRangeEnd - 1);
            } else {
                getReadings(sensors, properties.getNrOfReadPeriods());

            }
        } catch (Exception e) {
            //TODO MMo - Evaluate this workaround for forcing an exit, especially with regard to the exit code logged in stderr or where?
            //TODO MMo - Consider try/catch (!!!!check finally) to exit with another code
            e.printStackTrace();
            SpringApplication.exit(applicationContext);
            System.exit(1);
        }
        //TODO MMo - Evaluate this workaround for forcing an exit, especially with regard to the exit code logged in stderr or where?
        //TODO MMo - Consider try/catch (!!!!check finally) to exit with another code
        //TODO MMo - Deze exit is overbodig aangezien deze al in UvSensorGatewayApplication zit
//        SpringApplication.exit(applicationContext);
//        System.exit(0);
    }

    private void getReadings(List<Sensor> sensors, long nrOfPeriods) {
        log.info(">>>>>>>getReadings() started");

        // TODO !!!MMo - Code duplication
        double readFrequencyHertz = properties.getReader().getReadFrequencyHertz();
        //TODO MMo - check in JavaBox if 1000/(long)f really needs rounding the automatic cast does the same
        long msPerReading = Math.round(1000 / readFrequencyHertz);
        //TODO MMo - make naming consistent with PdO's choice or use an internal name (2x)
        double secondsPerReadingPeriod = properties.getReader().getSampleTimeSeconds();
        //TODO MMo - why long??
        double msPerReadPeriod = secondsPerReadingPeriod * 1000;
        //----

        long startTime = currentTimeMillis();
        long prevRespTimestamp = startTime;
        long sleepTimeInMs= 0;
        int nrOfReadings = 0;

        //---------------------------------
        //For each sensor start new Readings
        startNewReadings(sensors, startTime);

        //---------------------------------
        //The read loop
        //TODO !!!MMo - Condider rename to nrOfPeriodsPassed
        //TODO !!MMo - Consider while for readability
        for(int nrOfPeriodsRead = 0; nrOfPeriods <= 0 || nrOfPeriodsRead < nrOfPeriods; ) {

            for (Sensor sensor : sensors) {
                if(properties.isMockReadings()) {
                    int slaveId = sensor.getSlaveId();
                    int radiation = responseSequenceTest[currIdx[slaveId]];
                    if(++currIdx[slaveId] >= responseSequenceTest.length) currIdx[slaveId] = 0;
                    sensor.getSensorReadings().addReading(radiation);
                } else {
                    sensor.read();
                    ++nrOfReadings;
                }
            }
            long respTimestamp = currentTimeMillis();

            sleepTimeInMs = msPerReading - (respTimestamp - (prevRespTimestamp + sleepTimeInMs));
            // TODO !!!MMo - Consider refactoring to delay() having only one parameter
            delay(sleepTimeInMs);
            sleepTimeInMs = 0;
            prevRespTimestamp = respTimestamp;

            // TODO MMo - check this comment: ?? enough-readings for next line in data file
            long currentTime = currentTimeMillis();
            double nrOfReadingsPerReadingPeriod = secondsPerReadingPeriod * readFrequencyHertz;
            if(nrOfReadings >= nrOfReadingsPerReadingPeriod || currentTime - startTime >= msPerReadPeriod) {
                log.trace("currentTime={}", currentTime);
                log.trace("startTime  ={}", startTime);
                startTime = currentTimeMillis();
                nrOfPeriodsRead++;
                nrOfReadings = 0;
                log.trace("nrOfPeriodsRead={}", nrOfPeriodsRead);
                for (Sensor sensor : sensors) {
                    DataFileWriter dataFileWriter = applicationContext.getBean(
                            DataFileWriter.class, sensor.getDeviceName(), sensor.getSensorReadings().toImmutable());
                    dataFileWriter.start();
                }
                // TODO MMo - check this comment: For each sensor start new Readings
                startNewReadings(sensors, startTime);
            }
        }
    }

    private void delay(long startTime, long delayMillis) {
        long currTime;
        if (delayMillis > 0) {
            do {
                //TODO !!!!MMo - replace by common practice delay icw Thread.sleep() taking into account Thread.sleep()'s inaccuracy of +-50ms
                //Consider using System.nanos() for the last 10..20 millis
                //Add some trace logging before refactoring
                double d = 0;
                for (int i = 1; i < 100; i++) {
                    d += 100 / Math.PI;
                }
                currTime = currentTimeMillis();
            } while (currTime < startTime + delayMillis);
            // TODO !!!MMo -  Check measured diff
            log.trace(">>>>sleepTimeInMs={}, measured diff={}", delayMillis, currTime - startTime - delayMillis);
        } else {
            log.trace(">>>>sleepTimeInMs={}", delayMillis);
        }
    }

    private void delay(long delayMillis){
        long startTime = System.nanoTime();
        long currTime;
        if (delayMillis > 0) {
            do {
                //TODO !!!!MMo - replace by common practice delay icw Thread.sleep() taking into account Thread.sleep()'s inaccuracy of +-50ms
                //Consider using System.nanos() for the last 10..20 millis
                //Add some trace logging before refactoring
                double d = 0;
                for (int i = 1; i < 100; i++) {
                    d += 100 / Math.PI;
                }
                currTime = System.nanoTime();
            } while (currTime < startTime + delayMillis * 1000_000);
            // TODO !!!MMo -  Check measured diff
            log.trace(">>>>sleepTimeInMs={}, measured diff={}", delayMillis, currTime - startTime - delayMillis);
        } else {
            log.trace(">>>>sleepTimeInMs={}", delayMillis);
        }
    }

    private long currentTimeMillis() {
        return currentTimeNanos() / 1000_000;
    }

    // TODO MMo - ?Inline this method or keep it for readability
    private long currentTimeNanos() {
        // TODO MMo - Vwdr alle "wollige" finals
        Instant now = Instant.now(clock);
        return now.getEpochSecond() * 1000_000_000 + now.getNano();
    }

    private static void startNewReadings(List<Sensor> sensorList, long startTime) {
        // TODO MMo - refactor to method toUtc... oslt (also in DataFileWriter??)
        Instant instant = Instant.ofEpochMilli(startTime);
        //TODO MMo - Why pass timezone is it's UTC-milliseconds since epoch already?
        ZonedDateTime utcDateTime = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
        for (Sensor sensor : sensorList) {
            sensor.newReadings(utcDateTime);
        }
    }

    private List<Sensor> getSensors() {

        List<Sensor> sensors = null;

        try {
            if(properties.isMockSensorExplorer()) {
                sensors = getMockedSensorList();
            } else {
                sensors = sensorExplorer.findSensors();
            }
            // (***) throw new IOException("=====================>getSensors");
        } catch (Exception e) {
            // TODO MMo: Catching an java.io.IOException: Port [COM3] cannot be opened after [3] attempts - valid ports are: [COM1,COM2,COM3]
            // results in "Process finished with exit code 1", while throwing it inside this class (see  (***)) doesn't
            throw new RuntimeException(e);
        }
        return sensors;
    }


    private List<Sensor> getMockedSensorList() {
        List<Sensor> sensors;
        sensors = List.of(
            //TODO MMo - new J2ModMaster() should be be removed
            applicationContext.getBean(Sensor.class, SLAVE_ID_1,"23-0196")
//                , new Sensor(SLAVE_ID_6,"21-2066", null)
        );
        return sensors;
    }
}
