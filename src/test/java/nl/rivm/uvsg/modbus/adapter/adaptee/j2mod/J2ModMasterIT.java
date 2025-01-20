package nl.rivm.uvsg.modbus.adapter.adaptee.j2mod;

import nl.rivm.uvsg.configuration.Properties;
import nl.rivm.uvsg.jserialcomm.RS485SlaveCOM1_SlaveId01;
import nl.rivm.uvsg.modbus.adapter.ReadInputRegistersResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static nl.rivm.uvsg.modbus.adapter.adaptee.MasterTestUtils.assertResponseFromRS485SlaveCOM1_SlaveId01;

/**
 * TODO MMo - This test only runs if a COM1-COM2 pair is available, which is currently achieved using
 *            EterLogic Virtual Serial Ports Emulator and manually create this port pair.
 * TODO MMo - For the release version, this tests need to be disabled or moved, so they are not included
 *              in the maven build.
 */
//TODO MMo - see PrototypeScopedBeanFactoryIntegrationTest: 'prevent ...'
@SpringBootTest
@ContextConfiguration(classes = {Properties.class, J2ModService.class})
class J2ModMasterIT {

    @Autowired
    private J2ModService j2ModMaster;

    private static RS485SlaveCOM1_SlaveId01 slaveThread;

    @BeforeAll
    static void setUp() {
        slaveThread = new RS485SlaveCOM1_SlaveId01();
        slaveThread.start();
    }

    @Test
    void readInputRegisters() throws Exception {
        short slaveId = 1;
        int startingAddress = 2;
        int nrOfInputRegisters = 8;
        ReadInputRegistersResponse response = j2ModMaster.readInputRegisters(slaveId, startingAddress, nrOfInputRegisters);
        assertResponseFromRS485SlaveCOM1_SlaveId01(response, startingAddress, nrOfInputRegisters);
    }
}
