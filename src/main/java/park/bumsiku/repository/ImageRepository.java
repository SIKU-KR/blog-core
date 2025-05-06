package park.bumsiku.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
public class ImageRepository {

    private final S3Client s3;
    private final String bucket;
    private final String baseUrl;

    public ImageRepository(
            S3Client s3Client,
            @Value("${aws.s3.bucket}") String bucket,
            @Value("${aws.s3.bucket.url}") String baseUrl) {
        this.s3 = s3Client;
        this.bucket = bucket;
        this.baseUrl = baseUrl;
    }

    public String insert(String filename, byte[] webpBytes) {
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(filename)
                .contentType("image/webp")
                .contentLength((long) webpBytes.length)
                .acl("public-read")   // 퍼블릭 읽기 권한
                .build();
        s3.putObject(req, RequestBody.fromBytes(webpBytes));
        return String.format("%s/%s", baseUrl, filename);
    }
}
