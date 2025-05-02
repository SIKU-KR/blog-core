package park.bumsiku.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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