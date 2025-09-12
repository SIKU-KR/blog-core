package park.bumsiku.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import park.bumsiku.repository.PostRepository;
import park.bumsiku.utils.monitoring.LogExecutionTime;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LlmService {

    private final ChatClient chatClient;
    private final PostRepository postRepository;

    @LogExecutionTime
    @Transactional
    public String generateSummary(String text) {
        List<String> summaries = postRepository.findRecentSummaries(5);
        String examples = getSummaryExampleString(summaries);
        SystemMessage systemMessage = getSystemMessageToSummarize();
        UserMessage userMessage = getUserMessageToSummarize(examples, text);

        return chatClient.prompt()
                .messages(systemMessage, userMessage)
                .call().content();
    }

    private String getSummaryExampleString(List<String> summaries) {
        return summaries.stream()
                .map(s -> "\"" + s + "\"")
                .collect(Collectors.joining(", "));
    }

    private SystemMessage getSystemMessageToSummarize() {
        String msg = "당신은 주어진 한글 텍스트의 내용을 간결하게 요약하는 전문 요약가입니다.";
        return new SystemMessage(msg);
    }

    private UserMessage getUserMessageToSummarize(String summary, String text) {
        String userMessageTemplate = """
                ### 학습할 요약 예시 ###
                {examples}
                
                ---
                
                ### 실제 요약 작업 ###
                위 예시들과 아래 지침을 참고하여 다음 텍스트를 요약해 주세요.
                
                **텍스트:**
                {text}
                
                **지침:**
                - 가장 중요한 내용을 2-3개의 한글 문장으로 요약해 주세요.
                - 전체 요약은 150자 이내로 작성해 주세요.
                - 전문적이고 객관적인 톤을 유지해 주세요.
                
                **요약 결과:**
                """;

        PromptTemplate promptTemplate = new PromptTemplate(userMessageTemplate);
        return new UserMessage(promptTemplate.render(Map.of(
                "examples", summary,
                "text", text
        )));
    }

}
