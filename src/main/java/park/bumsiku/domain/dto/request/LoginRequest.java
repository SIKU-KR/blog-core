package park.bumsiku.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "password")
@Schema(description = "로그인 요청 데이터")
public class LoginRequest {

    @Schema(description = "사용자 아이디", example = "admin")
    private String username;

    @Schema(description = "비밀번호", example = "password")
    private String password;
}