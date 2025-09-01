package park.bumsiku.log.performance;

import org.slf4j.Logger;
import org.slf4j.MDC;
import park.bumsiku.log.MdcUtils;

import java.util.concurrent.TimeUnit;

public class PerformanceMonitor {

    private final Logger logger;
    private final PerformanceConfig config;

    public PerformanceMonitor(Logger logger, PerformanceConfig config) {
        this.logger = logger;
        this.config = config;
    }

    public void logIfSlow(long durationNanos, String className, String methodName, String operation) {
        long durationMs = TimeUnit.NANOSECONDS.toMillis(durationNanos);
        long threshold = config.getThreshold(operation);

        if (durationMs > threshold) {
            String requestId = MDC.get(MdcUtils.KEY_REQUEST_ID);
            logger.warn("Slow {} detected - RequestId: {}, {}.{} finished in {} ms",
                    operation, requestId, className, methodName, durationMs);
        }
    }
}