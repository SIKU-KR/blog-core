package park.bumsiku.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import park.bumsiku.config.ClockConfig;
import park.bumsiku.config.LoggingConfig;
import park.bumsiku.config.SecurityConfig;
import park.bumsiku.domain.dto.request.SummaryGenerationRequest;
import park.bumsiku.service.LlmService;
import park.bumsiku.utils.integration.DiscordWebhookCreator;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GptController.class)
@Import({SecurityConfig.class, ClockConfig.class, LoggingConfig.class})
@WithMockUser // secure /ai/** requires authentication
class GenerativeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LlmService llmService;

    @MockitoBean
    private DiscordWebhookCreator discordWebhookCreator;

    @Test
    @DisplayName("POST /ai/summary - success")
    void generateSummary_success() throws Exception {
        // given
        String input = "테스트 본문입니다";
        String expected = "요약 결과입니다";
        when(llmService.generateSummary(anyString())).thenReturn(expected);

        SummaryGenerationRequest req = SummaryGenerationRequest.builder()
                .text(input)
                .build();

        // when/then
        mockMvc.perform(post("/ai/summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.summary", is(expected)))
                .andExpect(jsonPath("$.error", is((Object) null)));
    }

    @Test
    @DisplayName("POST /ai/summary - service throws IllegalArgumentException -> 400")
    void generateSummary_serviceError_badRequest() throws Exception {
        // given
        when(llmService.generateSummary(anyString())).thenThrow(new IllegalArgumentException("잘못된 입력"));

        SummaryGenerationRequest req = SummaryGenerationRequest.builder()
                .text("invalid")
                .build();

        // when/then
        mockMvc.perform(post("/ai/summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", notNullValue()));
    }
}
