package park.bumsiku.utils.log;

/**
 * Constants used for logging configuration.
 */
public class LoggingConstants {

    /**
     * Threshold in milliseconds for considering a request as slow.
     * Requests taking longer than this will be logged at WARN level.
     */
    public static final long SLOW_REQUEST_THRESHOLD_MS = 500;

    /**
     * Header name for request ID.
     */
    public static final String HEADER_REQUEST_ID = "X-Request-ID";

    /**
     * Header name for forwarded IP.
     */
    public static final String HEADER_FORWARDED_FOR = "X-Forwarded-For";

    /**
     * Header name for real IP.
     */
    public static final String HEADER_REAL_IP = "X-Real-IP";

    /**
     * Header name for user agent.
     */
    public static final String HEADER_USER_AGENT = "User-Agent";

    /**
     * Header name for referer.
     */
    public static final String HEADER_REFERER = "Referer";

    /**
     * Value for unknown header values.
     */
    public static final String UNKNOWN = "unknown";

    // Private constructor to prevent instantiation
    private LoggingConstants() {
    }
}