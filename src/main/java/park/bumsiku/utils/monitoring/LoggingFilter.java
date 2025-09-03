package park.bumsiku.utils.monitoring;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    private static final long SLOW_REQUEST_THRESHOLD_MS = 500;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (isAsyncDispatch(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();

        try (MdcCloseable ignored = MdcCloseable.create()) {
            MdcUtils.setupMdc();
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            if (status >= 500) {
                log.error("HTTP Request Failed - {} {} - Status: {} - Duration: {}ms",
                        request.getMethod(), request.getRequestURI(), status, duration);
            } else if (duration > SLOW_REQUEST_THRESHOLD_MS || status >= 400) {
                log.warn("HTTP Request Slow/Warning - {} {} - Status: {} - Duration: {}ms",
                        request.getMethod(), request.getRequestURI(), status, duration);
            } else {
                log.info("HTTP Request Completed - {} {} - Status: {} - Duration: {}ms",
                        request.getMethod(), request.getRequestURI(), status, duration);
            }
        }
    }

    /**
     * Simple MDC utility for request tracking
     */
    public static class MdcUtils {
        public static final String KEY_REQUEST_ID = "requestId";

        static String setupMdc() {
            String requestId = UUID.randomUUID().toString();
            MDC.put(KEY_REQUEST_ID, requestId);
            return requestId;
        }

        static void clear() {
            MDC.clear();
        }
    }

    /**
     * Auto-closeable for automatic MDC cleanup
     */
    private static class MdcCloseable implements AutoCloseable {
        
        static MdcCloseable create() {
            return new MdcCloseable();
        }

        @Override
        public void close() {
            MdcUtils.clear();
        }
    }
}
