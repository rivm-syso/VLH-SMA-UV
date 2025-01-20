package nl.rivm.uvsg.modbus.adapter.adaptee.j2mod;

import com.fazecast.jSerialComm.SerialPort;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import nl.rivm.uvsg.configuration.Properties;
import nl.rivm.uvsg.modbus.adapter.InputRegister;
import nl.rivm.uvsg.modbus.adapter.ModbusException;
import nl.rivm.uvsg.modbus.adapter.ModbusService;
import nl.rivm.uvsg.modbus.adapter.ReadInputRegistersResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Service
public class J2ModService implements ModbusService {

    private final Properties properties;

    ModbusSerialMaster master;

    @PostConstruct
    private void connectModbusMaster() {

        SerialParameters params = new SerialParameters();
        //TODO MMo - ???Obtain these values from the configuration or only the baud rate??
        params.setPortName(properties.getRs485Port());
        params.setBaudRate(19200);
        params.setDatabits(8);
        params.setStopbits(SerialPort.ONE_STOP_BIT);
        params.setParity(SerialPort.EVEN_PARITY);

        master = new ModbusSerialMaster(params);
        try {
            master.connect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ReadInputRegistersResponse readInputRegisters(short slaveId, int startingAddress, int nrOfInputRegisters) throws Exception {

        try {
            com.ghgande.j2mod.modbus.procimg.InputRegister[] j2ModInputRegisters =
                    master.readInputRegisters(slaveId, startingAddress, nrOfInputRegisters);

            InputRegister[] inputRegisters = convertToInputRegisterArray(j2ModInputRegisters);
            return new J2modReadInputRegistersResponse(inputRegisters, startingAddress, nrOfInputRegisters);

        } catch (com.ghgande.j2mod.modbus.ModbusException e) {
            throw new ModbusException(e);
        }
    }

    private static InputRegister[] convertToInputRegisterArray(com.ghgande.j2mod.modbus.procimg.InputRegister[] j2ModInputRegisters) {
        List<J2modInputRegisterAdapter> inputRegistersAsList = new ArrayList<>();
        Arrays.stream(j2ModInputRegisters).forEach(inputRegister -> inputRegistersAsList.add(new J2modInputRegisterAdapter(inputRegister)));

        InputRegister[] j2modInputRegisterAdapters = inputRegistersAsList.toArray(new J2modInputRegisterAdapter[inputRegistersAsList.size()]);
        return j2modInputRegisterAdapters;
    }
}
