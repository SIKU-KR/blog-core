package park.bumsiku.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "content")
public class UpdatePostRequest {

    @NotBlank(message = "제목을 입력해주세요")
    @Size(min = 1, max = 100, message = "제목은 1자 이상 100자 이하로 입력해주세요")
    private String title;

    @NotBlank(message = "내용을 입력해주세요")
    @Size(min = 1, max = 10000, message = "내용은 1자 이상 10000자 이하로 입력해주세요")
    private String content;

    @NotBlank(message = "요약을 입력해주세요")
    @Size(min = 1, max = 200, message = "요약은 1자 이상 200자 이하로 입력해주세요")
    private String summary;

    private List<String> tags;
}
