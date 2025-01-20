package nl.rivm.uvsg;

import com.ghgande.j2mod.modbus.io.ModbusSerialTransport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static nl.rivm.uvsg.persistence.DataFileWriter.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
//TODO MMo - Use @TestPropertySource(properties = "name=value") and remove
// the annotation @ActiveProfiles and the profile-specific properties file
// 'application-test.properties'
@ActiveProfiles("test-nomocks")
class UvSensorGatewayIntegrationProductionLikeIT {

    private static final String DEVICE_NAME = "23-0196";

    @SpyBean
    private SensorReader sensorReader;

    @TempDir
    static Path tempDir;

    @BeforeAll
    static void setUp() {
        System.setProperty("localDataPath", tempDir.toString());
        ModbusSerialTransport modbusTransport = Mockito.mock(ModbusSerialTransport.class);
    }

    @Test
    void test() throws IOException {
        verify(sensorReader, times(1)).run();

        DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern(SUB_DIRECTORY_PATTERN);
        LocalDate now = LocalDate.now();
        String year = now.format(yearFormatter);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(FILENAME_DATETIME_PATTERN);
        String fileNameWithTimestamp = now.format(dateTimeFormatter);
        String fileName = String.format(FILENAME_FORMAT, DEVICE_NAME, fileNameWithTimestamp);

        String path = tempDir + "/" + year + "/" + DEVICE_NAME + "/";
        Path filePath = Paths.get(path + fileName);

        long expectedFileSize = Files.size(Paths.get("src/test/data/referenceDataFile"));
        long fileSize = Files.size(filePath);
        assertEquals(expectedFileSize, fileSize);
    }
}
