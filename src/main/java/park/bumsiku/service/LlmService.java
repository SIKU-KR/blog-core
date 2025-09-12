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
                당신은 한국어 전문 요약가입니다.
                목표: 사실 보존을 최우선으로 하되, 건조하지 않게 ‘부드러운 정보형’ 톤으로 요약합니다.
                톤 가이드:
                - 문장은 짧게, 구어체 한 끗는 허용하되 과한 감탄/수식은 금지
                - 독자가 얻는 이점을 한 번은 직접적으로 언급
                - 과장·추정 금지, 마케팅 문구 금지, 이모지 금지
                형식:
                - 2~3문장, 150자 이내
                - 머리말/꼬리말/마크다운/따옴표 금지(요약문만 출력)
                """;
        return new SystemMessage(msg);
    }

    private UserMessage getUserMessageToSummarize(String examples, String text) {
        String userMessageTemplate = """
                [스타일 예시(참고용): 문체/압축만 참고, 내용 전이는 금지]
                ----
                {examples}
                ----
                
                [요약 지침]
                1) 핵심 맥락 한 줄 후크 → 2) 변화/결과 → 3) 독자가 얻는 이점 순서 권장
                2) 구체명사·수치·날짜는 보존. 군더더기는 제거.
                3) 150자 초과 시 덜 중요한 수식·예시부터 걷어내세요.
                
                [입력]
                <<<
                {text}
                >>>
                
                [출력]
                - 요약문만 출력.
                - 요약 부적합 시: '요약 불가: <이유>' 한 줄만.
                """;
        return new UserMessage(new PromptTemplate(userMessageTemplate).render(Map.of(
                "examples", examples,
                "text", text
        )));
    }
}


