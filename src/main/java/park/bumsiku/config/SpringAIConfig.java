package park.bumsiku.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAIConfig {

    @Bean
    public ChatClient openAiChatClient(OpenAiChatModel chatModel) {
        OpenAiChatOptions defaultOptions = OpenAiChatOptions.builder()
                .model("gpt-5-mini")
                .temperature(1.0)
                .build();

        return ChatClient.builder(chatModel)
                .defaultOptions(defaultOptions)
                .build();
    }
}
