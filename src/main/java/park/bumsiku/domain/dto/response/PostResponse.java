package park.bumsiku.domain.dto.response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "content")
public class PostResponse {
    private int id;
    private String title;
    private String content;
    private String summary;
    private int categoryId;
    private List<String> tags;
    private String createdAt;
    private String updatedAt;
    private Long views;
}