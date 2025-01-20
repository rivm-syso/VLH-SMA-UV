package nl.rivm.uvsg.modbus.adapter.adaptee.modbus4j;

import nl.rivm.uvsg.modbus.adapter.InputRegister;

import static nl.rivm.uvsg.modbus.adapter.ModbusUtil.bytePairToInt;
import static nl.rivm.uvsg.modbus.adapter.ModbusUtil.bytePairToShort;

public class Modbus4jInputRegisterAdapter implements InputRegister {

    public Modbus4jInputRegisterAdapter(byte firstByte, byte secondByte) {
        this.valueAsBytePair[0] = firstByte;
        this.valueAsBytePair[1] = secondByte;
    }

    private final byte[] valueAsBytePair = new byte[2];

    @Override
    public byte[] getBytePair() {
        return valueAsBytePair;
    }

    @Override
    public int toShort() {
        return bytePairToShort(valueAsBytePair);
    }

    @Override
    public int toUnsignedShort() {
        return bytePairToInt(getBytePair());
    }
}
