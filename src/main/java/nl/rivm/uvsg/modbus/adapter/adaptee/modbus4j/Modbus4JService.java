package nl.rivm.uvsg.modbus.adapter.adaptee.modbus4j;

import com.fazecast.jSerialComm.SerialPort;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.msg.ReadInputRegistersRequest;
import com.serotonin.modbus4j.serial.SerialMaster;
import com.serotonin.modbus4j.serial.SerialPortWrapper;
import com.serotonin.modbus4j.serial.SerialPortWrapperImpl;
import lombok.extern.slf4j.Slf4j;
import nl.rivm.uvsg.modbus.adapter.ModbusService;
import nl.rivm.uvsg.modbus.adapter.ReadInputRegistersResponse;

import java.util.ArrayList;
import java.util.List;

//TODO !!!MMo Finish implementation esp. the Exception handling part
//TODO !!!MMo Remove ModBUs4J stuff vgd door commit-comment (of andere identifier) zdd dit weer teruggevonden kan worden
@Slf4j
public class Modbus4JService implements ModbusService {

    private final SerialPortWrapper serialPort;
    private final SerialMaster modbusMaster;

    public Modbus4JService(int commPortNumber) {

        serialPort = openSerialPort(
                commPortNumber,
                19200,
                8,
                SerialPort.ONE_STOP_BIT,
                SerialPort.EVEN_PARITY,
                180 * 24,
                20);

        ModbusFactory modbusFactory = new ModbusFactory();

        try {
            modbusMaster = (SerialMaster) modbusFactory.createRtuMaster(serialPort);
            //modbusMaster.setTimeout(5000);
            modbusMaster.setRetries(3);
            modbusMaster.init();

//TODO !!!MMo - Move this back to UVSGReader
//            if(mockSensorExplorer) {
//                sensorList = getMockedSensorList();
//            } else {
//                SensorExplorer sensorExplorer = new SensorExplorer(serialPort, modbusFactory);
//                sensorList = sensorExplorer.run();
//            }
//            for(Sensor sensor : sensorList) {
//                getSomeRealTimeDataRegisters(modbusMaster, sensor.getSlaveId());
//                getAllRegisters(modbusMaster, sensor.getSlaveId());
//            }
//            getReadings(modbusMaster, sensorList, SensorReadings.MILLISECONDS_PER_READING, SensorReadings.MILLISECONDS_PER_READ_PERIOD, 0);

            // TODO MMo GBC: klopt niet: bij failed to open port: modBusMaster=null=> NPE => al afgevangen
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ReadInputRegistersResponse readInputRegisters(short slaveId, int startingAddress, int nrOfInputRegisters) {

        com.serotonin.modbus4j.msg.ReadInputRegistersResponse modbus4jResponse = null;
        try {
            ReadInputRegistersRequest request = new ReadInputRegistersRequest(slaveId, startingAddress, nrOfInputRegisters);

            modbus4jResponse = (com.serotonin.modbus4j.msg.ReadInputRegistersResponse) modbusMaster.send(request);
        } catch (ModbusTransportException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (modbusMaster != null) {
                    modbusMaster.destroy();
                }
                serialPort.close();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }

        // TODO !!!MMo GBC
        if (!modbus4jResponse.isException()) {
            List<Modbus4jInputRegisterAdapter> inputRegistersAsList = new ArrayList<>();
            byte[] bytes = modbus4jResponse.getData();
            for (int i = 0; i < bytes.length; i += 2) {
                inputRegistersAsList.add(new Modbus4jInputRegisterAdapter(bytes[i], bytes[i + 1]));
            }
            Modbus4jInputRegisterAdapter[] InputRegisters = inputRegistersAsList.toArray(new Modbus4jInputRegisterAdapter[inputRegistersAsList.size()]);

            return new Modbus4jReadInputRegistersResponse(InputRegisters, startingAddress, nrOfInputRegisters);
        } else {
            throw new RuntimeException(modbus4jResponse.getExceptionMessage());
        }
    }

    public static SerialPortWrapper openSerialPort(int portNr, int baudRate,
                                                   int dataBits, int oneStopBit, int evenParity,
                                                   int nrOfRetries, int secondsPerRetry) {

        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // TODO MMo from >> Modbus4J - RS485 ModBus implementation
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // Set up Modbus Rs485 parameters
        SerialPortWrapper serialPort = new SerialPortWrapperImpl(
                "COM" + portNr,
                baudRate,
                dataBits,
                oneStopBit,
                evenParity);

        // TODO MMo: QAD-code
        for (int i = 0; i < nrOfRetries; i++) {
            try {
                serialPort.open();
                log.info(">>Opened serial port COM{} for Modbus-RS485 communication", portNr);
                return serialPort;
            } catch (Exception e) {
                if(i == nrOfRetries - 1) {
                    log.error(">>Failed to open serial port");
                    throw new RuntimeException("Failed to open serial port", e);
                }
                log.error(e.getMessage());
                try {
                    log.error("\n>>Serial port COM{} cannot be opened.\n" +
                                    ">>Try closing any apps using serial port COM{}\n" +
                                    ">>{}{} try to open port in {} seconds{}",
                            portNr, portNr, i+2, (i==0 ? "nd" : "th"), secondsPerRetry);
                    Thread.sleep(secondsPerRetry * 1000);
                } catch (InterruptedException ex) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(ex);
                }
            }
        }
        return null;
    }
}
