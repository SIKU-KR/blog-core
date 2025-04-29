package park.bumsiku.domain.dto;

import lombok.*;

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