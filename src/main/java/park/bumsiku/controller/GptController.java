package park.bumsiku.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import park.bumsiku.domain.dto.request.SlugGenerationRequest;
import park.bumsiku.domain.dto.request.SummaryGenerationRequest;
import park.bumsiku.domain.dto.response.Response;
import park.bumsiku.domain.dto.response.SlugGenerationResponse;
import park.bumsiku.domain.dto.response.SummaryGenerationResponse;
import park.bumsiku.service.LlmService;
import park.bumsiku.utils.monitoring.LogExecutionTime;

@Slf4j
@RestController
@RequestMapping("/ai")
@AllArgsConstructor
public class GptController implements GenerativeApi {

    private final LlmService llmService;

    @Override
    @PostMapping("/summary")
    @LogExecutionTime
    public Response<SummaryGenerationResponse> generateSummary(@RequestBody @Valid SummaryGenerationRequest request) {
        String summary = llmService.generateSummary(request.getText());
        SummaryGenerationResponse payload = SummaryGenerationResponse.builder()
                .summary(summary)
                .build();
        return Response.success(payload);
    }

    @Override
    @PostMapping("/slug")
    @LogExecutionTime
    public Response<SlugGenerationResponse> generateSummary(SlugGenerationRequest request) {
        String slug = llmService.generateSlug(request.getTitle(), request.getText());
        SlugGenerationResponse payload = SlugGenerationResponse.builder()
                .slug(slug)
                .build();
        return Response.success(payload);
    }
}

