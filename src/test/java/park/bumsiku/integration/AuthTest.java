package park.bumsiku.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import park.bumsiku.config.AbstractTestSupport;
import park.bumsiku.domain.dto.request.LoginRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthTest extends AbstractTestSupport {

    @Test
    public void testLoginSuccess() throws Exception {
        // Create login request with valid credentials
        LoginRequest loginRequest = LoginRequest.builder()
                .username("admin")
                .password("password")
                .build();

        // Perform login request and expect 200 OK with secure cookie
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("JSESSIONID"))
                .andExpect(cookie().secure("JSESSIONID", true));
    }

    @Test
    public void testLoginFailure() throws Exception {
        // Create login request with invalid credentials
        LoginRequest loginRequest = LoginRequest.builder()
                .username("admin")
                .password("wrongpassword")
                .build();

        // Perform login request and expect 401 Unauthorized
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
