package nl.rivm.uvsg.modbus.adapter.adaptee.j2mod;

import lombok.AllArgsConstructor;
import nl.rivm.uvsg.modbus.adapter.InputRegister;

@AllArgsConstructor
public class J2modInputRegisterAdapter implements InputRegister {

    private final com.ghgande.j2mod.modbus.procimg.InputRegister adaptee;

    @Override
    public int toShort() {
        return adaptee.toShort();
    }

    @Override
    public int toUnsignedShort() {
        return adaptee.toUnsignedShort();
    }

    @Override
    public byte[] getBytePair() {
        return adaptee.toBytes();
    }
}
