package park.bumsiku.log;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import static park.bumsiku.log.LoggingConstants.*;

/**
 * Utility class for managing MDC (Mapped Diagnostic Context) operations.
 * Encapsulates MDC key names and common operations.
 */
public class MdcUtils {
    // MDC key constants
    public static final String KEY_REQUEST_ID = "requestId";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_THREAD = "thread";
    public static final String KEY_HTTP_METHOD = "httpMethod";
    public static final String KEY_REQUEST_URI = "requestUri";
    public static final String KEY_QUERY_STRING = "queryString";
    public static final String KEY_PROTOCOL = "protocol";
    public static final String KEY_CLIENT_IP = "clientIp";
    public static final String KEY_USER_AGENT = "userAgent";
    public static final String KEY_REFERER = "referer";
    public static final String KEY_CONTENT_TYPE = "contentType";
    public static final String KEY_CONTENT_LENGTH = "contentLength";
    public static final String KEY_SESSION_ID = "sessionId";
    public static final String KEY_X_FORWARDED_FOR = "xForwardedFor";
    public static final String KEY_X_REAL_IP = "xRealIp";
    public static final String KEY_EXCEPTION = "exception";
    public static final String KEY_ERROR_MESSAGE = "errorMessage";
    public static final String KEY_RESPONSE_STATUS = "responseStatus";
    public static final String KEY_RESPONSE_DURATION = "responseDuration";

    /**
     * Initialize MDC with request information
     *
     * @param request The HTTP request
     * @param clock   Clock for timestamp generation (for testability)
     * @return The request ID used (either from X-Request-ID header or generated)
     */
    public static String setupMdc(HttpServletRequest request, Clock clock) {
        // Get or generate request ID
        String requestId = getOrGenerateRequestId(request);

        // Basic request info
        MDC.put(KEY_REQUEST_ID, requestId);
        MDC.put(KEY_TIMESTAMP, LocalDateTime.now(clock).format(DateTimeFormatter.ISO_DATE_TIME));
        MDC.put(KEY_THREAD, Thread.currentThread().getName());

        // HTTP request info
        MDC.put(KEY_HTTP_METHOD, request.getMethod());
        MDC.put(KEY_REQUEST_URI, request.getRequestURI());
        MDC.put(KEY_QUERY_STRING, request.getQueryString());
        MDC.put(KEY_PROTOCOL, request.getProtocol());

        // Client info
        String clientIp = getClientIpAddress(request);
        MDC.put(KEY_CLIENT_IP, clientIp);
        MDC.put(KEY_USER_AGENT, request.getHeader(HEADER_USER_AGENT));
        MDC.put(KEY_REFERER, request.getHeader(HEADER_REFERER));
        MDC.put(KEY_CONTENT_TYPE, request.getContentType());
        MDC.put(KEY_CONTENT_LENGTH, String.valueOf(request.getContentLengthLong()));

        // Session info (if available)
        if (request.getSession(false) != null) {
            MDC.put(KEY_SESSION_ID, request.getSession().getId());
        }

        // Custom headers
        String xForwardedFor = request.getHeader(HEADER_FORWARDED_FOR);
        if (xForwardedFor != null) {
            MDC.put(KEY_X_FORWARDED_FOR, xForwardedFor);
        }

        String xRealIp = request.getHeader(HEADER_REAL_IP);
        if (xRealIp != null) {
            MDC.put(KEY_X_REAL_IP, xRealIp);
        }

        return requestId;
    }

    /**
     * Get request ID from X-Request-ID header or generate a new UUID
     *
     * @param request The HTTP request
     * @return The request ID
     */
    public static String getOrGenerateRequestId(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HEADER_REQUEST_ID))
                .orElse(UUID.randomUUID().toString());
    }

    /**
     * Add exception information to MDC
     *
     * @param exception The exception
     */
    public static void putExceptionInfo(Exception exception) {
        MDC.put(KEY_EXCEPTION, exception.getClass().getSimpleName());
        MDC.put(KEY_ERROR_MESSAGE, exception.getMessage());
    }

    /**
     * Add response information to MDC
     *
     * @param status   HTTP response status
     * @param duration Request duration in milliseconds
     */
    public static void putResponseInfo(int status, long duration) {
        MDC.put(KEY_RESPONSE_STATUS, String.valueOf(status));
        MDC.put(KEY_RESPONSE_DURATION, String.valueOf(duration));
    }

    /**
     * Clear all MDC entries
     */
    public static void clear() {
        MDC.clear();
    }

    /**
     * Get client IP address from request headers or remote address
     *
     * @param request The HTTP request
     * @return The client IP address
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader(HEADER_FORWARDED_FOR);
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !UNKNOWN.equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader(HEADER_REAL_IP);
        if (xRealIp != null && !xRealIp.isEmpty() && !UNKNOWN.equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
