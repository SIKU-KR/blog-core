package park.bumsiku.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.client.ChatClient;

@Service
@AllArgsConstructor
public class LlmService {

    private final ChatClient chatClient;



}
