package park.bumsiku.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import park.bumsiku.domain.entity.Post;
import park.bumsiku.domain.entity.Tag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostSummaryResponse {
    private int id;
    private String slug;
    private String title;
    private String summary;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long views;
    private String canonicalPath;

    public static PostSummaryResponse from(Post post) {
        List<String> tagNames = post.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());

        return PostSummaryResponse.builder()
                .id(post.getId())
                .slug(post.getSlug())
                .title(post.getTitle())
                .summary(post.getSummary())
                .tags(tagNames)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .views(post.getViews())
                .canonicalPath("/posts/" + post.getSlug())
                .build();
    }
}
