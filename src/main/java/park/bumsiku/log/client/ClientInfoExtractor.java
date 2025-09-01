package park.bumsiku.log.client;

import jakarta.servlet.http.HttpServletRequest;

public interface ClientInfoExtractor {
    String extractClientIp(HttpServletRequest request);
}