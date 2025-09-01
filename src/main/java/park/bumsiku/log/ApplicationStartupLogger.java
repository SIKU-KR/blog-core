package park.bumsiku.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationStartupLogger implements ApplicationListener<ApplicationStartedEvent> {

    private final Environment environment;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        log.info("--------------------------------------------------------");
        log.info("              Application Started Successfully           ");
        log.info("--------------------------------------------------------");
        log.info("Java Version: {}", System.getProperty("java.version"));
        log.info("Spring Boot Version: {}", getClass().getPackage().getImplementationVersion());
        log.info("Active Profiles: {}", Arrays.toString(environment.getActiveProfiles()));
        log.info("Server Port: {}", environment.getProperty("server.port"));
        log.info("Database URL: {}", environment.getProperty("spring.datasource.url"));
        log.info("--------------------------------------------------------");
    }
} 