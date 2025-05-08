package park.bumsiku.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
    private int id;
    private String title;
    private String content;
    private int categoryId;
    private String createdAt;
    private String updatedAt;
}