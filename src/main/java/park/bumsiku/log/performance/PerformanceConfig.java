package park.bumsiku.log.performance;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Getter
@ConfigurationProperties(prefix = "logging.performance")
public class PerformanceConfig {

    private Map<String, Long> thresholds = Map.of(
            "method", 500L,
            "repository", 100L,
            "request", 500L
    );

    public long getThreshold(String operation) {
        return thresholds.getOrDefault(operation, 500L);
    }

    public Map<String, Long> getThresholds() {
        return thresholds;
    }

    public void setThresholds(Map<String, Long> thresholds) {
        this.thresholds = thresholds;
    }
}