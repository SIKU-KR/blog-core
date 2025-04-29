package park.bumsiku.domain.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
    private int id;
    private String title;
    private String content;
    private String createdAt;
    private String updatedAt;
}