package park.bumsiku.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ApplicationStartupLogger implements ApplicationListener<ApplicationStartedEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(ApplicationStartupLogger.class);

    private final Environment environment;

    public ApplicationStartupLogger(Environment environment) {
        this.environment = environment;
    }

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