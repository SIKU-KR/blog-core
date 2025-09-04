package park.bumsiku.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTagRequest {

    @NotBlank(message = "태그 이름을 입력해주세요")
    @Size(min = 1, max = 50, message = "태그 이름은 1자 이상 50자 이하로 입력해주세요")
    private String name;
}