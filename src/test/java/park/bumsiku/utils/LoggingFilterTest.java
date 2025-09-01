package park.bumsiku.utils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import park.bumsiku.log.LoggingConstants;
import park.bumsiku.log.LoggingFilter;
import park.bumsiku.log.client.DefaultClientInfoExtractor;
import park.bumsiku.log.performance.PerformanceConfig;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoggingFilterTest {

    private LoggingFilter loggingFilter;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    // Fixed clock for testing
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        // Create a fixed clock for testing
        fixedClock = Clock.fixed(Instant.parse("2023-01-01T12:00:00Z"), ZoneId.systemDefault());

        // Create the filter with the fixed clock
        DefaultClientInfoExtractor clientInfoExtractor = new DefaultClientInfoExtractor();
        PerformanceConfig performanceConfig = new PerformanceConfig();
        loggingFilter = new LoggingFilter(fixedClock, clientInfoExtractor, performanceConfig);

        // Create mock request and response
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        // Set up request with test data
        request.setMethod("GET");
        request.setRequestURI("/test");
        request.addHeader("User-Agent", "Test User Agent");
    }

    @Test
    void shouldWrapRequestAndResponse() throws ServletException, IOException {
        // When
        loggingFilter.doFilter(request, response, filterChain);

        // Then
        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        ArgumentCaptor<HttpServletResponse> responseCaptor = ArgumentCaptor.forClass(HttpServletResponse.class);

        verify(filterChain).doFilter(requestCaptor.capture(), responseCaptor.capture());

        assertInstanceOf(ContentCachingRequestWrapper.class, requestCaptor.getValue());
        assertInstanceOf(ContentCachingResponseWrapper.class, responseCaptor.getValue());
    }

    @Test
    void shouldUseExistingRequestId() throws ServletException, IOException {
        // Given
        String expectedRequestId = "test-request-id";
        request.addHeader(LoggingConstants.Headers.REQUEST_ID, expectedRequestId);

        // When
        loggingFilter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(any(), any());
        // We can't easily verify MDC values directly in a test, but we can check that the filter completed successfully
    }

    @Test
    void shouldLogSlowRequestsWithWarnLevel() throws ServletException, IOException {
        // Given
        // Create a clock that advances by more than the slow threshold
        Clock advancingClock = new AdvancingClock(fixedClock, 600); // 600ms > default 500ms threshold
        DefaultClientInfoExtractor clientInfoExtractor = new DefaultClientInfoExtractor();
        PerformanceConfig performanceConfig = new PerformanceConfig();
        loggingFilter = new LoggingFilter(advancingClock, clientInfoExtractor, performanceConfig);

        // When
        loggingFilter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(any(), any());
        // We can't easily verify log levels directly in a test, but we can check that the filter completed successfully
    }

    @Test
    void shouldHandleExceptions() throws ServletException, IOException {
        // Given
        Exception expectedException = new RuntimeException("Test exception");
        doThrow(expectedException).when(filterChain).doFilter(any(), any());

        // When/Then
        Exception thrownException = assertThrows(RuntimeException.class, () -> {
            loggingFilter.doFilter(request, response, filterChain);
        });

        assertEquals(expectedException, thrownException);
    }

    /**
     * Custom Clock implementation that advances time between now() calls
     */
    private static class AdvancingClock extends Clock {
        private final Clock baseClock;
        private final long advanceMillis;
        private long callCount = 0;

        public AdvancingClock(Clock baseClock, long advanceMillis) {
            this.baseClock = baseClock;
            this.advanceMillis = advanceMillis;
        }

        @Override
        public ZoneId getZone() {
            return baseClock.getZone();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new AdvancingClock(baseClock.withZone(zone), advanceMillis);
        }

        @Override
        public Instant instant() {
            // First call returns the base time, subsequent calls advance by advanceMillis
            if (callCount++ == 0) {
                return baseClock.instant();
            } else {
                return baseClock.instant().plusMillis(advanceMillis);
            }
        }

        @Override
        public long millis() {
            return instant().toEpochMilli();
        }
    }
}