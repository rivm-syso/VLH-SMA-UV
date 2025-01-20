package nl.rivm.uvsg;

import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;
import nl.rivm.uvsg.modbus.adapter.InputRegister;
import nl.rivm.uvsg.modbus.adapter.ModbusException;
import nl.rivm.uvsg.modbus.adapter.ModbusService;
import nl.rivm.uvsg.modbus.adapter.adaptee.j2mod.J2modInputRegisterAdapter;
import nl.rivm.uvsg.modbus.adapter.adaptee.j2mod.J2modReadInputRegistersResponse;
import nl.rivm.uvsg.sensor.Sensor;
import nl.rivm.uvsg.sensor.metadata.KippEnZonen_DeviceTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Random;

import static nl.rivm.uvsg.sensor.metadata.KippEnZonen_SMP3A.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorExplorerTest {

    private static final int START_ADDRESS = IO_DEVICE_TYPE.address;
    private static final int NR_OF_ADDRESSES = IO_SERIAL_NUMBER.address - START_ADDRESS + 1;

    @Mock
    ModbusService modbusService;

    @Mock
    ApplicationContext applicationContext;

    @InjectMocks
    SensorExplorer sensorExplorer;

    @Test
    void testDeviceAndAssertFields() throws Exception {

        mockNoDevicesInRange(2, SensorExplorer.MAXIMUM_NUMBER_OF_SLAVES);

        int slaveId = 1;
        getMockedResponseAndAssertFields(slaveId, KippEnZonen_DeviceTypes.UNKNOWN);
        getMockedResponseAndAssertFields(slaveId, KippEnZonen_DeviceTypes.SMP3A);
        getMockedResponseAndAssertFields(slaveId, KippEnZonen_DeviceTypes.SUVA);
        getMockedResponseAndAssertFields(slaveId, KippEnZonen_DeviceTypes.SUVE);
    }

    @Test
    void testDeviceListWithLength4() throws Exception {

        mockNoDevicesInRange(2, 5);
        mockNoDevicesInRange(8, SensorExplorer.MAXIMUM_NUMBER_OF_SLAVES - 1);

        mockDeviceResponse(1, 0, 0, 0);
        mockDeviceResponse(6, 0, 0, 0);
        mockDeviceResponse(7, 0, 0, 0);
        mockDeviceResponse(12, 0, 0, 0);

        List<Sensor> sensors = sensorExplorer.findSensors();
        assertEquals(4, sensors.size());
    }

    private void mockNoDevicesInRange(int lower, int upper) throws Exception {
        when(modbusService.readInputRegisters(shortThat(isInRange(lower, upper)), anyInt(), anyInt())).
                thenThrow(new ModbusException(new com.ghgande.j2mod.modbus.ModbusIOException()));
    }

    private void getMockedResponseAndAssertFields(int slaveId, KippEnZonen_DeviceTypes deviceType) throws Exception {

        Random random = new Random();
        int batchNumberRandom = random.nextInt(0x10000);
        int serialNumberRandom = random.nextInt(0x10000);
        mockDeviceResponse(slaveId, batchNumberRandom, serialNumberRandom, deviceType.value);

        when(applicationContext.getBean(eq(Sensor.class), anyShort(), anyString())).
                thenAnswer(invocation -> new Sensor(invocation.getArgument(1), invocation.getArgument(2)));

        List<Sensor> sensors = sensorExplorer.findSensors();
        assertEquals(1, sensors.size());

        String expectedDeviceName = deviceType.name + batchNumberRandom + serialNumberRandom;
        assertEquals(expectedDeviceName, sensors.get(0).getDeviceName());

        assertEquals(slaveId, sensors.get(0).getSlaveId());
    }

    private void mockDeviceResponse(int slaveId, int batchNr, int serialNr, int deviceType) throws Exception {
        J2modReadInputRegistersResponse inputRegistersResponse =
                createInputRegisterResponse(deviceType, batchNr, serialNr);

        when(modbusService.readInputRegisters((short) slaveId, START_ADDRESS, NR_OF_ADDRESSES)).
                thenReturn(inputRegistersResponse);
    }

    private static ArgumentMatcher<Short> isInRange(int start, int end) {
        return i -> (i >= (short) start && i <= (short) end);
    }

    private static J2modReadInputRegistersResponse createInputRegisterResponse(
            int deviceType, int batchNr, int serialNr) {

        InputRegister[] inputRegisters = new InputRegister[NR_OF_ADDRESSES];

        inputRegisters[IO_DEVICE_TYPE.address] = createRegister(deviceType);
        inputRegisters[IO_BATCH_NUMBER.address] = createRegister(batchNr);
        inputRegisters[IO_SERIAL_NUMBER.address] = createRegister(serialNr);

        return new J2modReadInputRegistersResponse(inputRegisters, START_ADDRESS, NR_OF_ADDRESSES);
    }

    private static J2modInputRegisterAdapter createRegister(int value) {
        return new J2modInputRegisterAdapter(new SimpleInputRegister(value));
    }
}
