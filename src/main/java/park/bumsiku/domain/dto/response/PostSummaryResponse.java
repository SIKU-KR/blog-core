package park.bumsiku.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import park.bumsiku.domain.entity.Post;

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

    public static PostSummaryResponse from(Post post) {
        return PostSummaryResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .summary(post.getSummary())
                .categoryId(post.getCategory().getId())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .views(post.getViews())
                .build();
    }

    // JPQL Constructor Expression을 위한 생성자 (Repository에서만 사용)
    public PostSummaryResponse(Integer id, String title, String summary, Integer categoryId,
                               LocalDateTime createdAt, LocalDateTime updatedAt, Long views) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.categoryId = categoryId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.views = views;
    }
}