package nl.rivm.uvsg.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.rivm.uvsg.configuration.Properties;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@RequiredArgsConstructor
public class DataFileWriter extends Thread {

    // TODO MMo - Consider moving everything to Properties
    // TODO MMo - Adjusting the file name pattern might be tested and communicated an extra feature
    // TODO MMo - Subdir pattern idem
    public static final String SUB_DIRECTORY_PATTERN = "yyyy";
    public static final String FILENAME_DATETIME_PATTERN = "yyyyMMdd_0000";
    public static final String EXTENSION = "csv";
    public static final String FILENAME_FORMAT = "%s-%s." + EXTENSION;

    private static final String READING_TIMESTAMP_PATTERN = "HH:mm";

    private static final int ADDITIONAL_DIGITS = 3;

    public static final String INVALID_PATH_MSG = "Invalid path '{}': {}";

    public static final String PLACEHOLDER_SENSOR_DEVICE_NAME = "#sensorDeviceName#";

    private final String sensorDeviceName;
    private final SensorReadings sensorReadings;

    @Autowired
    private Properties properties;

    @Override
    public void run() {
        writeSensorReadings();
    }

    // TODO MMo - Why synchronized?? Only as defensive coding: consider "call once"-pattern, ie throw exception on second call
    // TODO MMo - >>? "static style" => if ok, make all methods here static
    private synchronized void writeSensorReadings() {
        ZonedDateTime timeStamp = sensorReadings.getUtcDateTime();
        log.trace("Current UTC Time: {}", timeStamp);

        DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern(SUB_DIRECTORY_PATTERN);
        String year = timeStamp.format(yearFormatter);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(FILENAME_DATETIME_PATTERN);
        String fileNameWithTimestamp = timeStamp.format(dateTimeFormatter);

        writeFile(sensorReadings, year, fileNameWithTimestamp);
    }

    private void writeFile(SensorReadings sensorReadings, String year, String fileNameWithTimestamp) {
        String fileName = String.format(FILENAME_FORMAT, sensorDeviceName, fileNameWithTimestamp);
        log.trace("File name: {}", fileName);

        String localDataDir = properties.getLocalDataPath() + '/' + year + '/' + sensorDeviceName;
        log.trace("Local directory path: {}", localDataDir);

        log.trace("Find or create data directory path on local drive '{}'", localDataDir);
        try {
            findOrCreateDirectoryPath(localDataDir);
        } catch (FileWriterException e) {
            throw new RuntimeException(e);
        }

        String networkDataDir = properties.getNetworkDataPath() + '/' + year + '/' + sensorDeviceName;
        log.trace("Network directory path: {}", networkDataDir);

        log.trace("Find or create data directory path on network drive '{}'", networkDataDir);
        try {
            findOrCreateDirectoryPath(networkDataDir);
        } catch (FileWriterException e) {
            // TODO MMo - test verbosity of this log approach compared with e.printStackTrace
            log.error("Error finding or creating network path '{}' for '{}'", networkDataDir, fileName, e);
        }
        String localFilePath = localDataDir + "/" + fileName;
        log.trace("Writing to '{}'", localFilePath);
        try {
            writeToDataFile(sensorReadings, localFilePath);
        } catch (FileWriterException e) {
            log.error("Error writing to '{}'", localFilePath, e);
            throw new RuntimeException(e);
        }
        String networkFilePath = networkDataDir + "/" + fileName;
        log.trace("Writing to '{}'", networkFilePath);
        try {
            copyToNetworkDrive(localFilePath, networkFilePath);
        } catch (IOException e) {
            log.error("Error writing to '{}'", networkFilePath, e);
        }
    }

    private void writeToDataFile(SensorReadings sensorReadings, String filePath) throws FileWriterException {
        if(Files.notExists(Paths.get(filePath))) {
            BufferedWriter writer = createDataFile(filePath, sensorReadings);
            appendToDataFileAndClose(writer, sensorReadings);
        } else {
            appendToDataFileAndClose(filePath, sensorReadings);
        }
    }

    private void copyToNetworkDrive(String localFilePath, String networkFilePath) throws IOException {
        CopyOption[] options = {
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES,
                LinkOption.NOFOLLOW_LINKS };
        Files.copy(Path.of(localFilePath), Path.of(networkFilePath), options);
    }

    private BufferedWriter createDataFile(String filePath, SensorReadings sensorReadings) throws FileWriterException {
        try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE_NEW);
            log.trace("Created new data file '{}' for writing header", filePath);
            writeHeader(writer, sensorReadings);
            return writer;
        } catch (InvalidPathException e) {
            log.error(INVALID_PATH_MSG, filePath, e.getMessage());

            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new FileWriterException("Error creating or writing to " + filePath, e);
        }
    }

    private void writeHeader(BufferedWriter writer, SensorReadings sensorReadings) throws IOException {
        String formatLine1 = "!Radiometer (DeviceType-BatchYear-SerialNr): %s";
//        String formatLine2 = "!Read frequency: %s Hz";
//        String formatLine3 = "!Read period duration: %s seconds";
//        String formatLine4 = "!Number of readings per line: %s";
//        String formatLine5 = "!Labels:";
//        String formatLine6 = "!Time: reception time of the first reading in a read period";
//        String formatLine7 = "!Radiation: temperature compensated radiation in W/m² (Net radiation for SGR) averaged over the reading period";
//        String formatLine8 = "!Std: standard deviation of the readings in that reading period";
//        String formatLine9 = "!#Readings: number of readings completed in that reading period";
//        String formatLine10 = "!";
        String formatLine11 = "!%s%12s%12s%12s";

        // TODO !!!MMo Code Duplication???
        // TODO !!!MMo - consider using a a Java builtin constant io 1000, also in other places (see delay())
        double secondsPerReadingPeriod = properties.getReader().getSampleTimeSeconds();
        double readFrequencyHertz = properties.getReader().getReadFrequencyHertz();

        double samplesPerReadingPeriod = secondsPerReadingPeriod * readFrequencyHertz;
        double msPerReadPeriod = secondsPerReadingPeriod * 1000;

        // TODO !!!MMo - gebruik een util method die getest is op alle decimal, thousand-seperator perikelen, etc.
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);

//        writeLine(writer, formatLine1, sensorDeviceName);
        for(String headerLine : properties.getHeaderLines()) {
            if(headerLine.contains(PLACEHOLDER_SENSOR_DEVICE_NAME)) {
                headerLine = headerLine.replace(PLACEHOLDER_SENSOR_DEVICE_NAME, sensorDeviceName);
            }
            writeLine(writer, "!%s", headerLine);
        }
//        writeLine(writer, formatLine2, decimalFormat.format(readFrequencyHertz));
//        writeLine(writer, formatLine3, decimalFormat.format(secondsPerReadingPeriod));
//        writeLine(writer, formatLine4, decimalFormat.format(samplesPerReadingPeriod));
//        writeLine(writer, formatLine5);
//        writeLine(writer, formatLine6);
//        writeLine(writer, formatLine7);
//        writeLine(writer, formatLine8);
//        writeLine(writer, formatLine9);
//        writeLine(writer, formatLine10);
        // TODO MMo - Consider parameterising these strings to avoid code duplication
        writeLine(writer, formatLine11, "Time", "Radiation", "Std", "#Readings");
    }

    private void appendToDataFileAndClose(String filePath, SensorReadings sensorReadings) throws FileWriterException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.APPEND)) {
            log.trace("Opened '{}' for appending", filePath);
            appendToDataFileAndClose(writer, sensorReadings);
        } catch (IOException e) {
            throw new FileWriterException("Error appending to" + filePath, e);
        }
    }

    private void appendToDataFileAndClose(BufferedWriter writer, SensorReadings sensorReadings) throws FileWriterException {
        try {
            log.trace("Writing to data file '{}'", writer);
            writeSensorReadings(writer, sensorReadings);
        } catch (IOException e) {
            throw new FileWriterException("Error appending to " + writer, e);
        }
    }

    private static void writeSensorReadings(BufferedWriter writer, SensorReadings sensorReadings) throws IOException {

        // TODO !!!MMo - Refactor
        int scaleFactor = sensorReadings.getScaleFactor() == null ? 0 : sensorReadings.getScaleFactor();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(READING_TIMESTAMP_PATTERN);
        String formattedTimestamp = sensorReadings.getUtcDateTime().format(formatter);

        log.info(">>>>>>>>>>>>sensorReadings.getSize()={}", sensorReadings.getSize());
        String dataLineFormat = "%s%12s%12s%12d";
        writeLine(writer, dataLineFormat,
                formattedTimestamp,
                scaleAndFormat(sensorReadings.getDeviceName(), sensorReadings.getAverage(), scaleFactor, ADDITIONAL_DIGITS),
                scaleAndFormat(sensorReadings.getDeviceName(), sensorReadings.getStandardDeviation(), scaleFactor, ADDITIONAL_DIGITS),
                sensorReadings.getSize());
    }

    private static void findOrCreateDirectoryPath(String localDataDir) throws FileWriterException {
        Path path;
        try {
            path = Paths.get(localDataDir);
        } catch (InvalidPathException e) {
            log.error(INVALID_PATH_MSG, localDataDir, e.getMessage());
            throw new RuntimeException(e);
        }
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                log.error("Error creating path '{}': {}", localDataDir, e.getMessage());
                throw new FileWriterException(e);
            }
            log.info("Created path '{}'", localDataDir);
        } else {
            log.trace("Found path '{}'", localDataDir);
        }
    }

    private static void writeLine(BufferedWriter writer, String format, Object... args) throws IOException {
        writer.write(String.format(Locale.US, format, args));
        writer.newLine();
        writer.flush();
    }

    // TODO !!MMo - naar Util verplaatsen (zie ModBusMasterUtil)
    private static String scaleAndFormat(String deviceName, double value, int scaleFactor, int additionalDigits) {

        double scaledValue = value / (int)Math.pow(10, scaleFactor);

        // TODO !!!MMo - util createNoneLocalSpecificFormatter(decimalSeparator, numberOfFractionDigits)
        // TODO MMo - unit test and document all requirements of the createNoneLocalSpecificFormatter
        DecimalFormat noneLocalSpecificFormatter = new DecimalFormat();
        noneLocalSpecificFormatter.setMaximumFractionDigits(scaleFactor + additionalDigits);
        noneLocalSpecificFormatter.setMinimumFractionDigits(scaleFactor + additionalDigits);
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        noneLocalSpecificFormatter.setDecimalFormatSymbols(decimalFormatSymbols);
        noneLocalSpecificFormatter.setGroupingUsed(false);

        String formattedScaledValue = noneLocalSpecificFormatter.format(scaledValue);
        log.trace(">>>>{}: scaleAndFormat(value={}, scaleFactor={}, additionalDigits={}) => scaledValue={} => formattedScaledValue={}",
                deviceName, value, scaleFactor, additionalDigits, scaledValue, formattedScaledValue);
        return formattedScaledValue;
    }
}
