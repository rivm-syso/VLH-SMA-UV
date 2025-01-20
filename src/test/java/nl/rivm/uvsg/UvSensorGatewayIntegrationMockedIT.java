package nl.rivm.uvsg;

import nl.rivm.uvsg.mockedclock.MockedClockFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.*;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static nl.rivm.uvsg.mockedclock.MockedClockFactory.*;
import static nl.rivm.uvsg.persistence.DataFileWriter.FILENAME_DATETIME_PATTERN;
import static nl.rivm.uvsg.persistence.DataFileWriter.FILENAME_FORMAT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Import(MockedClockFactory.class)
@SpringBootTest
//TODO MMo - Use @TestPropertySource(properties = "name=value") and remove
// the annotation @ActiveProfiles and the profile-specific properties file
// 'application-test.properties'
@ActiveProfiles("test")
class UvSensorGatewayIntegrationMockedIT {

    private static final String DEVICE_NAME = "23-0196";

    @Autowired
    Clock clock;

    @SpyBean
    private SensorReader sensorReader;

    @TempDir
    static Path tempDir;

    @BeforeAll
    static void setUp() {
        System.setProperty("localDataPath", tempDir.toString());
    }

    @Test
    void test() throws IOException {

        verify(sensorReader, times(1)).run();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(FILENAME_DATETIME_PATTERN);
        LocalDate fixedDate = LocalDate.of(YEAR, MONTH, DAY);
        String fileNameWithTimestamp = fixedDate.format(dateTimeFormatter);
        String fileName = String.format(FILENAME_FORMAT, DEVICE_NAME, fileNameWithTimestamp);

        String path = tempDir + "/" + YEAR + "/" + DEVICE_NAME + "/";
        Path actualFilePath = Paths.get(path + fileName);

        assertTrue(Files.exists(actualFilePath));
        Path referenceFilePath = Paths.get("src/test/data/referenceDataFile");
        long expectedFileSize = Files.size(referenceFilePath);
        long fileSize = Files.size(actualFilePath);
        assertEquals(expectedFileSize, fileSize);
        long mismatchPosition = Files.mismatch(referenceFilePath, actualFilePath) + 1;
        if(mismatchPosition > 0) {
            CopyOption[] options = {
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES,
                    LinkOption.NOFOLLOW_LINKS };
            String outputDataFileName = "src/test/data/integrationTestOutputDataFile";
            Path filePathForComparison = Paths.get(outputDataFileName);
            Files.copy(actualFilePath, filePathForComparison, options);
            String msg = "There is a mismatch at position %d between output data file [%s] resulting from " +
                    "the test and the reference file [%s]";
            fail(String.format(msg, mismatchPosition, filePathForComparison, referenceFilePath));
        }
    }
}
