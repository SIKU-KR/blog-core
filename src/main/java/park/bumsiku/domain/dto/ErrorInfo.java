package park.bumsiku.domain.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorInfo {
    private int status;
    private String message;
}