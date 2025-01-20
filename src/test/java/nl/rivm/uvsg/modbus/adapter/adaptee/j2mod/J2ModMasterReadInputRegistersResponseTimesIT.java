package nl.rivm.uvsg.modbus.adapter.adaptee.j2mod;

import lombok.extern.slf4j.Slf4j;
import nl.rivm.uvsg.configuration.Properties;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * This test is intended for production like environment where a Modbus device with the tested slaveId is connected
 * via the rs485 port configured in the application properties file.
 */
//TODO MMo - see PrototypeScopedBeanFactoryIntegrationTest: 'prevent ...'
@Slf4j
@SpringBootTest
@ContextConfiguration(classes = {Properties.class, J2ModService.class})
class J2ModMasterReadInputRegistersResponseTimesIT {

    public static final int NR_OF_REQUESTS = 120;
    @Autowired
    private J2ModService j2ModMaster;

    @ParameterizedTest
    @CsvSource(value = {
            "1, 2, 8"
    })
    void readInputRegisters(short slaveId, int startingAddress, int nrOfInputRegisters) throws Exception {
        long start = System.nanoTime();
        for (int i = 0; i < NR_OF_REQUESTS; i++) {
            j2ModMaster.readInputRegisters(slaveId, startingAddress, nrOfInputRegisters);
        }
        long end = System.nanoTime();
        long responseTimeMs = (end - start) / 1000_000;
        long avgResponseTimeMs = responseTimeMs / NR_OF_REQUESTS;
        log.info("j2ModMaster.readInputRegisters(slaveId={}, startingAddress={}, nrOfInputRegisters={}) called {} times in {} ms => average response time: {} ms",
                slaveId, startingAddress, nrOfInputRegisters, NR_OF_REQUESTS, responseTimeMs, avgResponseTimeMs);
    }
}
