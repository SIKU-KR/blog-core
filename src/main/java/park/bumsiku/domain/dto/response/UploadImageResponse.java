package park.bumsiku.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadImageResponse {
    private String url;
    private String fileName;
    private String mimeType;
    private long size;
    private long timestamp;
}