package nl.rivm.uvsg.modbus.adapter.adaptee;

import nl.rivm.uvsg.modbus.adapter.ReadInputRegistersResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

public class MasterTestUtils {

    public static void assertResponseFromRS485SlaveCOM1_SlaveId01(ReadInputRegistersResponse response, int startingAddress, int nrOfInputRegisters) {
        assertEquals(1, response.getRegister(startingAddress));
        assertEquals(0, response.getRegister(startingAddress + 1));
        assertEquals(4, response.getRegister(startingAddress + 2));
        assertEquals(997, response.getRegister(startingAddress + 3));
        assertEquals(997, response.getRegister(startingAddress + 4));
        assertEquals(0, response.getRegister(startingAddress + 5));
        assertEquals(248, response.getRegister(startingAddress + 6));
        assertEquals(234, response.getRegister(startingAddress + 7));
        int address = startingAddress + 8;
        // TODO MMo - Use auto formatting and line width 160 for the whole code base
        IllegalArgumentException illegalArgumentException = assertThrowsExactly(IllegalArgumentException.class, () -> response.getRegister(address));
        assertEquals(String.format("Illegal register address %d with %d addresses in the response and startingAddress=%d",
                        address, nrOfInputRegisters, startingAddress), illegalArgumentException.getMessage());
    }
}
