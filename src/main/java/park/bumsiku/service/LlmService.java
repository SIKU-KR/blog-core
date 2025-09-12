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
        String msg = """
                당신은 **기술 블로그** 글을 요약하는 카피라이터입니다.
                의뢰인은 복잡한 기술 문제를 명쾌하게 풀어내는 '개발자'이자, 동료의 성장을 돕는 '친절한 테크 리드'입니다.
                당신의 요약문은 개발을 공부하는 사람들이 원문 게시물을 읽고 싶게 만들어야 합니다.
                아래 예시 Summary와 유사한 톤앤매너로 작성하세요.
                
                [최종 출력 조건]
                - 2문장 또는 3문장으로 작성.
                - 각 문장은 반드시 8~12 단어 사이여야 하며, 어길 경우 잘못된 출력으로 간주한다.
                - 친근하지만 전문적인 어조, 기술적 사실 전달을 중심으로 작성
                - 오직 요약문만 출력할 것. 불필요한 접두사/설명/메타텍스트는 금지.
                - 문장은 마침표로 끝내고 줄바꿈 없이 이어쓴다.
                """;
        return new SystemMessage(msg);
    }

    private UserMessage getUserMessageToSummarize(String examples, String text) {
        String userMessageTemplate = """
                [스타일 참고 예시]
                ----
                {examples}
                ----
                
                [요약할 원문]
                <<<
                {text}
                >>>
                
                [요약 결과]
                """;
        return new UserMessage(new PromptTemplate(userMessageTemplate).render(Map.of(
                "examples", examples,
                "text", text
        )));
    }
}


