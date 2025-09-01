package park.bumsiku.log;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;

import java.util.Optional;
import java.util.UUID;

import static park.bumsiku.log.LoggingConstants.Headers;

/**
 * Simplified MDC utility - only handles request ID for tracing
 */
public class MdcUtils {

    public static final String KEY_REQUEST_ID = "requestId";

    /**
     * Setup MDC with only request ID for simple request tracing
     */
    public static String setupMdc(HttpServletRequest request) {
        String requestId = getOrGenerateRequestId(request);
        MDC.put(KEY_REQUEST_ID, requestId);
        return requestId;
    }

    /**
     * Get request ID from X-Request-ID header or generate a new UUID
     */
    public static String getOrGenerateRequestId(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(Headers.REQUEST_ID))
                .orElse(UUID.randomUUID().toString());
    }

    /**
     * Clear all MDC entries
     */
    public static void clear() {
        MDC.clear();
    }
}
