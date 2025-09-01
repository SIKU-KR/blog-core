package park.bumsiku.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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
            MdcUtils.setupMdc(request);
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
}
