package park.bumsiku.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import park.bumsiku.domain.dto.request.SummaryGenerationRequest;
import park.bumsiku.domain.dto.response.Response;
import park.bumsiku.domain.dto.response.SummaryGenerationResponse;

@Tag(name = "Generative AI", description = "생성형 AI 관련 API")
public interface GenerativeApi {

    @Operation(
            summary = "텍스트 요약 생성",
            description = "입력된 텍스트를 기반으로 요약문을 생성합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Response.class)
            )
    )
    @PostMapping("/ai/summary")
    Response<SummaryGenerationResponse> generateSummary(@RequestBody SummaryGenerationRequest request);

}
