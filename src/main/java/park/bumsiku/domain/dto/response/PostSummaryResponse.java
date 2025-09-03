package park.bumsiku.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostSummaryResponse {
    private int id;
    private String title;
    private String summary;
    private int categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long views;
}