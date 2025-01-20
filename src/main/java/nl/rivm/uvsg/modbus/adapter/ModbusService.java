package nl.rivm.uvsg.modbus.adapter;

public interface ModbusService {

    public ReadInputRegistersResponse readInputRegisters(short slaveId, int startingAddress, int nrOfInputRegisters) throws Exception;
}
