package nl.rivm.uvsg.modbus.adapter.adaptee.modbus4j;

import nl.rivm.uvsg.modbus.adapter.InputRegister;
import nl.rivm.uvsg.modbus.adapter.ReadInputRegistersResponse;

public class Modbus4jReadInputRegistersResponse extends ReadInputRegistersResponse {

    public Modbus4jReadInputRegistersResponse(InputRegister[] inputRegisters, int startingAddress, int nrOfAddresses) {
        super(inputRegisters, startingAddress, nrOfAddresses);
    }
}
