package park.bumsiku.domain.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private int id;
    private String name;
    private int orderNum;
    private LocalDateTime createdAt;
}