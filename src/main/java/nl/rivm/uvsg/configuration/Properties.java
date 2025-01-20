package nl.rivm.uvsg.configuration;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import nl.rivm.util.JarManifestProperties;
import nl.rivm.util.SpringBootUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Slf4j
@Getter
@Setter
@Component
@Validated
@ConfigurationProperties
public class Properties {

    @Autowired
    private Environment environment;

    private boolean mockSensorExplorer;
    private boolean mockReadings;
    private int nrOfReadPeriods;

    // TODO MMo - in IntgrTests, that are run with @ContextConfiguration(classes = {Properties.class}),
    //  fields without the @Value annotation remain null.
    // ==> Alt: replace the use of @ContextConfiguration by setting the properties individually in J2ModMasterIntegrationTest and J2ModMasterIntegrationTest...
    //Btr: Mock SensorReader en weglaten @ContextConfiguration (toepassing van deze laatste os momenteel nl nog vaag)
    @Value("${rs485Port}")
    @NotBlank(message = ">>>>>>>>>>>RS485 port not set. Include the 'rs485Port' in the application properties, eg. COM1")
    private String rs485Port;

    @Getter
    @Setter
    public Reader reader;

    @Getter
    @Setter
    public static class Reader {
        @Positive
        // TODO MMo - why double?
        private double readFrequencyHertz;
        @Positive
        // TODO MMo - why double?
        private double sampleTimeSeconds;
    }

    @NotBlank(message = ">>>>>>>>>>>Local path to store data files not set. Include in data.LocalDataPath in the application properties.")
    private String localDataPath;
    @NotBlank(message = ">>>>>>>>>>>Network path to store data files not set. Include in data.LocalDataPath in the application properties.")
    private String networkDataPath;

    private List<String> headerLines;

    @PostConstruct
    private void logEnvironmentProperties() {
        SpringBootUtilities.logEnvironmentProperties(environment, log);
    }

    @PostConstruct
    private void logJarManifestProperties() {
        JarManifestProperties.logProperties();
    }
}
