package nl.rivm.uvsg.sensor;

import com.ghgande.j2mod.modbus.ModbusIOException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.rivm.uvsg.modbus.adapter.ModbusService;
import nl.rivm.uvsg.modbus.adapter.ReadInputRegistersResponse;
import nl.rivm.uvsg.persistence.SensorReadings;
import nl.rivm.uvsg.sensor.metadata.KippEnZonen_SMP3A;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;

@Slf4j
@RequiredArgsConstructor
public class Sensor {

    // TODO MMo - Remove @Getter ?on slaveId after removal of mockRadiation
    @Getter
    private final short slaveId;
    @Getter
    private final String deviceName;

    @Autowired
    private ModbusService modbusService;

    @Getter
    private SensorReadings sensorReadings;

    /*
         TODO MMo - Currently register 2 to 9 are read to support integration testing using the Modbus slaves in nl.rivm.uvsg.jserialcomm.
                    Therefore read(), for convenience, also reads and sets the scaleFactor.
                    The Slaves need to be extended, so they are able to respond to any register-request.
         */
    public void read() {
        int startAddress = 2;
        int numberOfRegisters = 8;
        try {
            ReadInputRegistersResponse response = modbusService.readInputRegisters(slaveId, startAddress, numberOfRegisters);
            sensorReadings.addReading(response.getRegister(KippEnZonen_SMP3A.IO_SENSOR1_DATA.address));
            if (sensorReadings.getScaleFactor() == null) {
                sensorReadings.setScaleFactor(response.getRegister(KippEnZonen_SMP3A.IO_SCALE_FACTOR.address));
            }
        } catch (Exception e) {
            // TODO !!!!MMo - Fix this, as it is not independent of the library!!!
            // TODO !!!MMo - we need the exact cause to be a timeout, not just an ModbusIOException
            // TODO !!!MMo - of is dit juist de robuustheid die we willen??? en dient er slechts een error gelogd te worden
            // TODO !!!MMo - ieg een error loggen
            log.trace(">>>>>>>>>>>>Catched Exception {} - '{}'", e, e.getMessage());
            if (!(e.getCause() instanceof ModbusIOException)) {
                throw new RuntimeException(e);
            }
        }
    }

    public void newReadings(ZonedDateTime utcDateTime) {
        sensorReadings = new SensorReadings(utcDateTime, deviceName);
    }
//
//    private static void getRegisters(int startOffset, int numberOfRegisters, int slaveId) throws ModbusTransportException {
//
//        ReadInputRegistersRequest request = new ReadInputRegistersRequest(slaveId, startOffset, numberOfRegisters);
//
//        ReadInputRegistersResponse response = (ReadInputRegistersResponse)modbusService.send(request);
//
//        // TODO MMo GBC
//        if (!response.isException()) {
//
//            //log.info("ReadInputRegistersResponse: " + response.getShortData());
//
//            short operationalMode = response.getShortData()[IO_OPERATIONAL_MODE - startOffset];
//            log.info(">>{}: {}", IO_OPERATIONAL_MODE_DESCRIPTION, toBinaryString(operationalMode));
//
//            short statusFlags = response.getShortData()[IO_STATUS_FLAGS - startOffset];
//            log.info(">>{}: {}", IO_STATUS_FLAGS_DESCRIPTION, toBinaryString(statusFlags));
//
//            int sensorScaleFactor = response.getShortData()[IO_SCALE_FACTOR - startOffset];
//            String formattedReading = sensorRegisterToFormattedFloat(response, IO_SENSOR1_DATA, startOffset, sensorScaleFactor);
//            log.info(">>{}: {}", IO_SENSOR1_DATA_DESCRIPTION, formattedReading);
//
//            String formattedBodyTemperature = sensorRegisterToFormattedFloat(response, IO_BODY_TEMPERATURE, startOffset, 1);
//            log.info(">>{}: {}", IO_BODY_TEMPERATURE_DESCRIPTION, formattedBodyTemperature);
//
//            String formattedExternalPower = sensorRegisterToFormattedFloat(response, IO_EXT_POWER_SENSOR, startOffset, 1);
//            log.info(">>{}: {}", IO_EXT_POWER_SENSOR_DESCRIPTION, formattedExternalPower);
//        } else {
//            log.error("Error: " + response.getExceptionMessage());
//        }
//    }
//
//    private static String sensorRegisterToFormattedFloat(ReadInputRegistersResponse response, int registerIdx, int registerIdxOffset, int sensorScaleFactor) {
//        int sensorReading = response.getShortData()[registerIdx - registerIdxOffset];
//        return formatAsFLoat(sensorReading, sensorScaleFactor);
//    }

}
