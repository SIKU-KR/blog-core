package park.bumsiku.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequest {

    @NotBlank(message = "댓글 내용을 입력해주세요")
    @Size(min = 1, max = 500, message = "댓글은 1자 이상 500자 이하로 입력해주세요")
    private String content;

    @NotBlank(message = "작성자 이름을 입력해주세요")
    @Size(min = 2, max = 20, message = "작성자 이름은 2자 이상 20자 이하로 입력해주세요")
    private String author;
}