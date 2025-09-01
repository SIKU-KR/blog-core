package park.bumsiku.log.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import park.bumsiku.log.client.ClientInfoExtractor;
import park.bumsiku.log.client.DefaultClientInfoExtractor;
import park.bumsiku.log.performance.PerformanceConfig;

import java.time.Clock;

@Configuration
public class LoggingConfig {

    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    @ConditionalOnMissingBean(ClientInfoExtractor.class)
    public ClientInfoExtractor clientInfoExtractor() {
        return new DefaultClientInfoExtractor();
    }

    @Bean
    @ConditionalOnMissingBean(PerformanceConfig.class)
    public PerformanceConfig performanceConfig() {
        return new PerformanceConfig();
    }
}