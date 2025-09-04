package park.bumsiku.domain.dto.response;

import lombok.*;
import park.bumsiku.domain.entity.Tag;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagResponse {

    private Integer id;
    private String name;
    private String createdAt;
    private Long postCount;

    public static TagResponse from(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .createdAt(tag.getCreatedAt().toString())
                .postCount((long) tag.getPosts().size())
                .build();
    }

    public static TagResponse fromWithoutPostCount(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .createdAt(tag.getCreatedAt().toString())
                .postCount(0L)
                .build();
    }
}