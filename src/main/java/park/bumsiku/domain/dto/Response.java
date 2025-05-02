package park.bumsiku.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Response<T> {
    private boolean success;
    private T data;
    private ErrorInfo error;

    public static <T> Response<T> success(T data) {
        return Response.<T>builder()
                .success(true)
                .data(data)
                .error(null)
                .build();
    }

    public static <T> Response<T> error(int status, String message) {
        return Response.<T>builder()
                .success(false)
                .data(null)
                .error(new ErrorInfo(status, message))
                .build();
    }
}