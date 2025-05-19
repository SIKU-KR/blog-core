package park.bumsiku.utils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) {
        try {
            // timestamp - logback에서 기본 제공하지만 MDC에도 추가
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
            MDC.put("timestamp", timestamp);

            // thread 정보 추가
            MDC.put("thread", Thread.currentThread().getName());

            // 요청 ID 생성 (단일 요청 식별용)
            String requestId = UUID.randomUUID().toString();
            MDC.put("requestId", requestId);

            // 클라이언트 정보 MDC에 추가
            MDC.put("clientIp", request.getRemoteAddr());
            MDC.put("userAgent", request.getHeader("User-Agent"));
            MDC.put("requestUrl", request.getRequestURI());
            MDC.put("requestMethod", request.getMethod());

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            MDC.clear();
        }
    }
}
