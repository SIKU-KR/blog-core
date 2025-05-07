package park.bumsiku.integration;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MvcResult;
import park.bumsiku.config.AbstractTestSupport;
import park.bumsiku.domain.dto.request.LoginRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class AuthTest extends AbstractTestSupport {

    @Test
    public void testLoginSuccess() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("admin")
                .password("password")
                .build();

        MvcResult result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        MockHttpServletResponse response = result.getResponse();

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        // 세션이 생성되었는지 확인
        assertThat(result.getRequest().getSession(false)).isNotNull();

        // SecurityContext가 세션에 저장되었는지 확인
        Object securityContext = result.getRequest().getSession().getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertThat(securityContext).isNotNull();
    }

    @Test
    public void testLoginFailure() throws Exception {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .username("admin")
                .password("wrongpassword")
                .build();

        // Act
        MvcResult result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        // Assert
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }
}
