package nl.rivm.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
abstract class PropertiesLogger {

    protected static void log(String propertiesFilePath, String... keys) {

        Properties properties = loadProperties(propertiesFilePath);
        log.info(">>>>>>>>>>logProperties({}, {})", propertiesFilePath, keys);
        if(keys == null) {
            log.info("Properties={}", properties.toString());
        } else {
            for (String keyStr : keys) {
                if(properties.containsKey(keyStr)) {
                    String value = properties.getProperty(keyStr);
                    log.info("{}={}", keyStr, value);
                } else {
                    log.warn("Key '{}' not found in '{}'", keyStr, propertiesFilePath);
                }
            }
        }
    }

    private static Properties loadProperties(String propertiesFilePath) {
        Properties properties;
        try (InputStream input = PropertiesLogger.class.getClassLoader().getResourceAsStream(propertiesFilePath)) {
            if (input == null) {
                throw new RuntimeException(propertiesFilePath + " not found");
            }
            properties = new Properties();
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

}
