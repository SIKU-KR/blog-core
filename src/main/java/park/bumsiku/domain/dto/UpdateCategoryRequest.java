package park.bumsiku.domain.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCategoryRequest {

    @NotNull(message = "Category ID cannot be null")
    private Integer id;

    @NotBlank(message = "Category cannot be blank")
    private String category;

    @NotNull(message = "Order cannot be null")
    private Integer order;
}