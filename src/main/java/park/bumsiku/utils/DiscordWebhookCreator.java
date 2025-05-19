package park.bumsiku.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;


@Component
public class DiscordWebhookCreator {

    @Value("${discord.url}")
    private String apiUrl;

    private static final Logger log = LoggerFactory.getLogger(DiscordWebhookCreator.class);

    private final RestTemplate restTemplate;

    public DiscordWebhookCreator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Async
    public void sendMessage(String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> payload = new HashMap<>();
        payload.put("content", message);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);

        try {
            restTemplate.postForEntity(apiUrl, entity, String.class);
            log.info("Message sending initiated to discord: " + message);
        } catch (Exception e) {
            log.error("Error sending message to discord: " + e.getMessage(), e);
        }
    }
}
