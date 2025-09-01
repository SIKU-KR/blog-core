package park.bumsiku.log;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * Simple MDC utility - generates unique request ID for tracing
 */
public class MdcUtils {

    public static final String KEY_REQUEST_ID = "requestId";

    /**
     * Setup MDC with a new unique request ID
     */
    public static String setupMdc() {
        String requestId = UUID.randomUUID().toString();
        MDC.put(KEY_REQUEST_ID, requestId);
        return requestId;
    }

    /**
     * Clear all MDC entries
     */
    public static void clear() {
        MDC.clear();
    }
}
