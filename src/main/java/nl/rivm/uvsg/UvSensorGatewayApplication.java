package nl.rivm.uvsg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@Slf4j
@RequiredArgsConstructor
@SpringBootApplication
public class UvSensorGatewayApplication {

    public static void main(String[] args) {

        log.info(String.format(">>>>>>>>>>>>main()-SpringApplication.run(UvSensorGatewayApplication.class, args=%s)", (args.length == 0) ? new String[]{"[]"} : args));
        ApplicationContext applicationContext = SpringApplication.run(UvSensorGatewayApplication.class, args);
        //TODO MMo - We need this to ensure exit, whenever SensorReader.run() ends, because no device were found or the nrOfReadPerid is limitted.
        SpringApplication.exit(applicationContext);
        System.exit(0);
    }
}
