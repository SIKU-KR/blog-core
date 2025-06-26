package park.bumsiku.utils.log;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

public class RequestResponseUtils {

    /**
     * Wraps the request with ContentCachingRequestWrapper if it's not already wrapped.
     *
     * @param request The HTTP request
     * @return The wrapped request
     */
    public static HttpServletRequest wrapRequest(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper) {
            return request;
        }
        return new ContentCachingRequestWrapper(request);
    }

    /**
     * Wraps the response with ContentCachingResponseWrapper if it's not already wrapped.
     *
     * @param response The HTTP response
     * @return The wrapped response
     */
    public static HttpServletResponse wrapResponse(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper) {
            return response;
        }
        return new ContentCachingResponseWrapper(response);
    }

    /**
     * Copies the response body to the output stream if the response is wrapped.
     * This is necessary because ContentCachingResponseWrapper buffers the response.
     *
     * @param response The HTTP response
     */
    public static void copyBodyToResponse(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper wrapper) {
            try {
                wrapper.copyBodyToResponse();
            } catch (Exception e) {
                throw new RuntimeException("Failed to copy response body", e);
            }
        }
    }

    // Private constructor to prevent instantiation
    private RequestResponseUtils() {
    }
}