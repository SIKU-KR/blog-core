package park.bumsiku.log;

/**
 * Constants used for logging configuration.
 */
public final class LoggingConstants {

    public static final class Headers {
        public static final String REQUEST_ID = "X-Request-ID";
        public static final String FORWARDED_FOR = "X-Forwarded-For";
        public static final String REAL_IP = "X-Real-IP";
        public static final String USER_AGENT = "User-Agent";
        public static final String REFERER = "Referer";

        private Headers() {
        }
    }

    public static final class Values {
        public static final String UNKNOWN = "unknown";

        private Values() {
        }
    }

    public static final class Operations {
        public static final String METHOD_EXECUTION = "method";
        public static final String DB_QUERY = "repository";
        public static final String HTTP_REQUEST = "request";

        private Operations() {
        }
    }

    private LoggingConstants() {
    }
}