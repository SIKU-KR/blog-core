package park.bumsiku.domain.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummaryGenerationRequest {
    @NotBlank(message = "제목을 입력해주세요")
    String text;
}
