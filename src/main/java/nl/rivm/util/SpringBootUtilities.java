package nl.rivm.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

@Slf4j
public class SpringBootUtilities {

    public static void logEnvironmentProperties(Environment environment, Logger log) {

        if (environment instanceof StandardEnvironment) {
            StandardEnvironment standardEnvironment = (StandardEnvironment) environment;
            log.info(">>>>>>>>>>logEnvironmentProperties() - StandardEnvironment injected: {}", environment.toString());
            int cntOriginTrackedMapPropertySources = 0;
            for (PropertySource<?> propertySource : standardEnvironment.getPropertySources()) {
                log.info(">>>>>>>>>>PropertySource={}", propertySource.getName());

                if (propertySource instanceof OriginTrackedMapPropertySource) {
                    Map<String, String> propertyMap = (Map<String, String>)propertySource.getSource();
                    log.info(">>>>>>>>>>Property Map {}:", ++cntOriginTrackedMapPropertySources);
                    for(String propertyName : propertyMap.keySet()) {
                        log.info("{}={}", propertyName, propertyMap.get(propertyName));
                    }
                }
            }
        } else {
            log.info(">>>>>>>>>>logEnvironmentProperties() - environment not of type StandardEnvironment injected: {}", environment.toString());
        }
    }
}
