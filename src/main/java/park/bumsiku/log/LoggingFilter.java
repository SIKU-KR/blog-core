package park.bumsiku.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;

import static park.bumsiku.log.LoggingConstants.SLOW_REQUEST_THRESHOLD_MS;

@Component
@AllArgsConstructor
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private final Clock clock;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (isAsyncDispatch(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = clock.millis();
        HttpServletRequest wrappedRequest = RequestResponseUtils.wrapRequest(request);
        HttpServletResponse wrappedResponse = RequestResponseUtils.wrapResponse(response);

        try (MdcCloseable ignored = MdcCloseable.create()) {
            MdcUtils.setupMdc(wrappedRequest, clock);
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } catch (Exception e) {
            MdcUtils.putExceptionInfo(e);
            // Re-throw the exception to be handled by the global exception handler
            throw e;
        } finally {
            long duration = clock.millis() - startTime;
            int status = wrappedResponse.getStatus();
            MdcUtils.putResponseInfo(status, duration);

            Level level = determineLogLevel(status, duration);

            // Centralized, structured logging at the end of the request
            logger.atLevel(level)
                    .setMessage("HTTP Request Completed")
                    .log();

            RequestResponseUtils.copyBodyToResponse(wrappedResponse);
        }
    }

    private Level determineLogLevel(int status, long duration) {
        if (status >= 500) {
            return Level.ERROR;
        } else if (duration > SLOW_REQUEST_THRESHOLD_MS || status >= 400) {
            return Level.WARN;
        } else {
            return Level.INFO;
        }
    }
}
