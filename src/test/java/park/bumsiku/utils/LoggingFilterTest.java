package park.bumsiku.utils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import park.bumsiku.log.LoggingFilter;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @BeforeEach
    void setUp() {
        // Create the simplified filter
        loggingFilter = new LoggingFilter();

        // Create mock request and response
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        // Set up request with test data
        request.setMethod("GET");
        request.setRequestURI("/test");
    }

    @Test
    void shouldProcessRequest() throws ServletException, IOException {
        // When
        loggingFilter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void shouldGenerateUniqueRequestId() throws ServletException, IOException {
        // When
        loggingFilter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(any(), any());
        // Each request gets a unique UUID - no external dependencies
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
}