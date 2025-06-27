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

import static park.bumsiku.log.LoggingConstants.HEADER_USER_AGENT;
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

        HttpServletRequest wrappedRequest = RequestResponseUtils.wrapRequest(request);
        HttpServletResponse wrappedResponse = RequestResponseUtils.wrapResponse(response);

        long startTime = clock.millis();

        try (MdcCloseable ignored = MdcCloseable.create()) {
            // requestId is used by 'logback-spring.xml'
            String requestId = MdcUtils.setupMdc(wrappedRequest, clock);

            logger.info("HTTP Request Started - {} {} from {}",
                    wrappedRequest.getMethod(),
                    wrappedRequest.getRequestURI(),
                    MdcUtils.getClientIpAddress(wrappedRequest));

            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } catch (Exception e) {
            MdcUtils.putExceptionInfo(e);
            logger.error("HTTP Request Failed - {} {}", request.getMethod(), request.getRequestURI(), e);
            throw e;
        } finally {
            long duration = clock.millis() - startTime;
            MdcUtils.putResponseInfo(wrappedResponse.getStatus(), duration);

            Level level = duration > SLOW_REQUEST_THRESHOLD_MS ? Level.WARN : Level.INFO;
            logger.atLevel(level)
                    .setMessage("HTTP Request - {} {} - Status: {} - Duration: {}ms - IP: {} - UserAgent: {}")
                    .addArgument(wrappedRequest.getMethod())
                    .addArgument(wrappedRequest.getRequestURI())
                    .addArgument(wrappedResponse.getStatus())
                    .addArgument(duration)
                    .addArgument(MdcUtils.getClientIpAddress(wrappedRequest))
                    .addArgument(wrappedRequest.getHeader(HEADER_USER_AGENT))
                    .log();

            RequestResponseUtils.copyBodyToResponse(wrappedResponse);
        }
    }
}
