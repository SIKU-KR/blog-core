package park.bumsiku.log.client;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import park.bumsiku.log.LoggingConstants;

@Component
public class DefaultClientInfoExtractor implements ClientInfoExtractor {

    @Override
    public String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader(LoggingConstants.Headers.FORWARDED_FOR);
        if (isValidIp(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader(LoggingConstants.Headers.REAL_IP);
        if (isValidIp(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !LoggingConstants.Values.UNKNOWN.equalsIgnoreCase(ip);
    }
}