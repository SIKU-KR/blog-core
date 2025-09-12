package park.bumsiku.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import park.bumsiku.config.AbstractTestSupport;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser // secure /ai/** requires authentication
public class AiTest extends AbstractTestSupport {

    @Test
    @DisplayName("[IT] POST /ai/summary - success")
    void generateSummary_success() throws Exception {
        // given
        String expected = "요약 결과입니다";
        when(llmService.generateSummary(anyString())).thenReturn(expected);

        String body = "{\n  \"text\": \"테스트 본문입니다\"\n}";

        // when/then
        mockMvc.perform(post("/ai/summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.summary", is(expected)));
    }

    @Test
    @DisplayName("[IT] POST /ai/summary - service error -> 400")
    void generateSummary_serviceError_badRequest() throws Exception {
        // given
        when(llmService.generateSummary(anyString())).thenThrow(new IllegalArgumentException("유효하지 않은 요청"));

        String body = "{\n  \"text\": \"bad\"\n}";

        // when/then
        mockMvc.perform(post("/ai/summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", is(400)))
                .andExpect(jsonPath("$.error.message", notNullValue()));
    }
}
