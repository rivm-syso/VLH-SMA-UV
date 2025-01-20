package nl.rivm.uvsg;

import com.ghgande.j2mod.modbus.ModbusIOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.rivm.uvsg.modbus.adapter.ModbusService;
import nl.rivm.uvsg.modbus.adapter.ReadInputRegistersResponse;
import nl.rivm.uvsg.sensor.Sensor;
import nl.rivm.uvsg.sensor.metadata.KippEnZonen_DeviceTypes;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static nl.rivm.uvsg.sensor.metadata.KippEnZonen_SMP3A.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class SensorExplorer {

    public static final short SLAVE_ID_RANGE_START = 1;

    //TODO MMo - This should be configurable with default=247, provided that for requesting the device-identification registers a small timeout can be used (see notes)
    public static final short MAXIMUM_NUMBER_OF_SLAVES = 12;

    private final ApplicationContext applicationContext;
    private final ModbusService modbusService;

    public List<Sensor> findSensors() {

        short slaveIdRangeEnd = SLAVE_ID_RANGE_START + MAXIMUM_NUMBER_OF_SLAVES;
        List<Sensor> sensorList = new ArrayList<>();

        log.info("Start scanning modbus addresses from {} to {}", SLAVE_ID_RANGE_START, slaveIdRangeEnd - 1);
        long startTime = System.currentTimeMillis();

        for (short slaveId = SLAVE_ID_RANGE_START; slaveId < slaveIdRangeEnd; ++slaveId) {

            String deviceName = getDeviceNameFromModbusSlave(modbusService, slaveId);
            if(deviceName != null) {
                Sensor sensor = applicationContext.getBean(Sensor.class, slaveId, deviceName);
                sensorList.add(sensor);
            }
        }
        log.info("Scanned Modbus address range {} to {} and found {} modbus slaves in {} ms.", SLAVE_ID_RANGE_START, slaveIdRangeEnd - 1, sensorList.size(), System.currentTimeMillis() - startTime);
        return sensorList;
    }

    private static String getDeviceNameFromModbusSlave(ModbusService modbusService, short slaveId) {

        ReadInputRegistersResponse response;

        int deviceTypeAddress = IO_DEVICE_TYPE.address;
        int batchNumberAddress = IO_BATCH_NUMBER.address;
        int serialNumberAddress = IO_SERIAL_NUMBER.address;

        // TODO MMo - Consider a generic method that accepts a set i.o. a range of registers
        // TODO MMo - We need this anyway when register-retrieval is configured
        // TODO MMo - Check Sensor for a generic method for ob
        // TODO MMo - Put this code in readInputRegisters()
        int startAddress = Math.min(Math.min(deviceTypeAddress, batchNumberAddress), serialNumberAddress);
        int endAddress = Math.max(Math.max(deviceTypeAddress, batchNumberAddress), serialNumberAddress);

        try {
            int nrOfRegisters = endAddress - startAddress + 1;
            response = modbusService.readInputRegisters(slaveId, startAddress, nrOfRegisters);

        } catch (Exception e) {
            // TODO !!!!MMo - Fix this, as it is not independent of the library!!!
            // TODO !!!MMo - we need the exact cause to be a timeout, not just an ModbusIOException
            // TODO !!!MMo - ieg een error loggen
            log.trace(">>>>>>>>>>>>Catched Exception {} - '{}'", e, e.getMessage());
            if (!(e.getCause() instanceof ModbusIOException)) {
                throw new RuntimeException(e);
            } else {
                return null;
            }
        }
        int deviceTypeId = response.getRegisterUnsigned(deviceTypeAddress);

        // TODO !!!MMo: make this code independent of the nrOf types in KippEnZonen_DeviceTypes
        // TODO !!!MMo: Btr: device types in configuratie opnemen
        String prefix = switch (deviceTypeId) {
            case 602 -> KippEnZonen_DeviceTypes.SMP3A.name;
            case 629 -> KippEnZonen_DeviceTypes.SUVE.name;
            case 625 -> KippEnZonen_DeviceTypes.SUVA.name;
            default ->
                // TODO MMo - Btr: SlaveN-DeviceType<Nr>Unknown (zie KippEnZonen_DeviceTypes)
                    "DeviceTypeUnknown";
        };
        String deviceName = String.format(prefix + "%02d%05d",
                response.getRegisterUnsigned(batchNumberAddress), response.getRegisterUnsigned(serialNumberAddress));
        log.info("Found device: {} (deviceTypeId={})", deviceName, deviceTypeId);
        return deviceName;
    }
}
