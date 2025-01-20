package nl.rivm.uvsg.modbus.adapter;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public abstract class ReadInputRegistersResponse {

    private final InputRegister[] inputRegisters;
    private final int startingAddress;
    private final int nrOfAddresses;

    //TODO MMo - nrOfAddresses and inputRegisters.length seam to be equal
    //TODO MMo - obscure class?

    public int getRegister(int address) {
        int relativeAddress = toRelativeAddress(address);
        // TODO !!!MMo choose the right method based on  the register's type found in the configuration.
        // TODO !!!MMo - for DeviceType, BatchNr and SerialNr toUnsignedShort must be called!!!!
        return inputRegisters[relativeAddress].toShort();
    }
    public int getRegisterUnsigned(int address) {
        int relativeAddress = toRelativeAddress(address);
        // TODO !!!MMo choose the right method based on  the register's type found in the configuration.
        // TODO !!!MMo - for DeviceType, BatchNr and SerialNr toUnsignedShort must be called!!!!
        return inputRegisters[relativeAddress].toUnsignedShort();
    }

    private int toRelativeAddress(int address) {
        int relativeAddress = address - startingAddress;
        if(relativeAddress < 0 || relativeAddress + 1 > inputRegisters.length) {
            String errorMsg = String.format("Illegal register address %d with %d addresses in the response and startingAddress=%d",
                    address, nrOfAddresses, startingAddress);
            throw new IllegalArgumentException(errorMsg);
        }
        return relativeAddress;
    }
}
