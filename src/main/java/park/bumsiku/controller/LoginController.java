package park.bumsiku.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import park.bumsiku.domain.dto.request.LoginRequest;
import park.bumsiku.utils.validation.DiscordWebhookCreator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Tag(name = "Authentication", description = "인증 관련 API")
@RestController
@AllArgsConstructor
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    private final AuthenticationManager authenticationManager;
    private final DiscordWebhookCreator discordWebhookCreator;

    @Operation(
            summary = "로그인",
            description = "사용자 아이디와 비밀번호로 로그인하여 세션을 생성합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "로그인 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            }
    )
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Explicitly create a session to ensure a cookie is set
            HttpSession session = request.getSession(true);
            log.info("Created session with ID: {} for user: {}", session.getId(), loginRequest.getUsername());

            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );

            log.info("Login successful for user: {}", loginRequest.getUsername());
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
            discordWebhookCreator.sendMessage(String.format("🔑 사용자 '%s'이 %s %s에 로그인했습니다.",
                    loginRequest.getUsername(),
                    now.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")),
                    now.format(DateTimeFormatter.ofPattern("HH시 mm분 ss초")))
            );
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.warn("Login failed for user: {} - Reason: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Operation(
            summary = "세션 상태 확인",
            description = "현재 세션이 유효한지 확인합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "세션이 유효함"),
                    @ApiResponse(responseCode = "401", description = "세션이 유효하지 않음", content = @Content)
            }
    )
    @GetMapping("/session")
    public ResponseEntity<Void> checkSession(HttpServletRequest request) {
        log.info("Checking session status");
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY) != null) {
            log.info("Session is valid with ID: {}", session.getId());
            return ResponseEntity.ok().build();
        } else {
            log.info("Session is invalid or expired");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}

