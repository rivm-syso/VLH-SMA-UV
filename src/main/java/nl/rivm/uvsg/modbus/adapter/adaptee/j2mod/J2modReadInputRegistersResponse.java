package nl.rivm.uvsg.modbus.adapter.adaptee.j2mod;

import nl.rivm.uvsg.modbus.adapter.InputRegister;
import nl.rivm.uvsg.modbus.adapter.ReadInputRegistersResponse;

public class J2modReadInputRegistersResponse extends ReadInputRegistersResponse {

    public J2modReadInputRegistersResponse(InputRegister[] inputRegisters, int startingAddress, int nrOfAddresses) {
        super(inputRegisters, startingAddress, nrOfAddresses);
    }
}
