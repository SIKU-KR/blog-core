package park.bumsiku.domain.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private int id;
    private String authorName;
    private String content;
    private String createdAt;
}