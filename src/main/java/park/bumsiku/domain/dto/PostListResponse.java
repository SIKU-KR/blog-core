package park.bumsiku.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostListResponse {
    private List<PostSummaryResponse> content;
    private int totalElements;
    private int pageNumber;
    private int pageSize;
}