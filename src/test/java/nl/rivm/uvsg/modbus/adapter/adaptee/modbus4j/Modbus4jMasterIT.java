package nl.rivm.uvsg.modbus.adapter.adaptee.modbus4j;

import nl.rivm.uvsg.jserialcomm.RS485SlaveCOM1_SlaveId01;
import nl.rivm.uvsg.modbus.adapter.ReadInputRegistersResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static nl.rivm.uvsg.modbus.adapter.adaptee.MasterTestUtils.assertResponseFromRS485SlaveCOM1_SlaveId01;

class Modbus4jMasterIT {

    private static Modbus4JService modbus4jMaster;
    private static RS485SlaveCOM1_SlaveId01 slaveThread;

    @BeforeAll
    static void setUp() throws Exception {
        slaveThread = new RS485SlaveCOM1_SlaveId01();
        slaveThread.start();
        modbus4jMaster = new Modbus4JService(2);
    }

    @Test
    void openSerialPort() {
        //TODO !!!MMo
    }

    @Test
    void readInputRegisters() {
        short slaveId = 1;
        int startingAddress = 2;
        int nrOfInputRegisters = 8;
        ReadInputRegistersResponse response = modbus4jMaster.readInputRegisters(slaveId, startingAddress, nrOfInputRegisters);
        assertResponseFromRS485SlaveCOM1_SlaveId01(response, startingAddress, nrOfInputRegisters);
    }
}
