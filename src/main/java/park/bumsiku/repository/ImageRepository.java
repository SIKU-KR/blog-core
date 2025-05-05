package park.bumsiku.repository;

import org.springframework.stereotype.Component;

@Component
public class ImageRepository {

    private final String baseUrl = "";

    public String insert(String filename, byte[] webpBytes) {

        return filename;
    }
}
