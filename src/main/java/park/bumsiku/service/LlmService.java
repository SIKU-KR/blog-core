package park.bumsiku.service;

import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import park.bumsiku.utils.monitoring.LogExecutionTime;

import java.util.Map;

@Service
@AllArgsConstructor
public class LlmService {

    private final ChatClient chatClient;

    @LogExecutionTime
    public String generateSummary(String text) {
        SystemMessage systemMessage = getSystemMessageToSummarize();
        UserMessage userMessage = getUserMessageToSummarize(text);

        return chatClient.prompt().messages(systemMessage, userMessage).call().content();
    }

    private SystemMessage getSystemMessageToSummarize() {
        String msg = """
                당신은 **기술 블로그** 글을 요약하는 카피라이터입니다.
                당신의 요약문은 개발을 공부하는 사람들이 원문 게시물을 읽고 싶게 만들어야 합니다.
                
                [최종 출력 조건]
                - 2문장 또는 3문장으로 작성.
                - 각 문장은 반드시 8~12 단어 사이여야 하며, 어길 경우 잘못된 출력으로 간주한다.
                - 자연스럽게 구어체로 작성하되 과도한 감탄/수식 금지
                - 오직 요약문만 출력할 것. 불필요한 접두사/설명/메타텍스트는 금지.
                - 문장은 마침표로 끝내고 줄바꿈 없이 이어쓴다.
                """;
        return new SystemMessage(msg);
    }

    private UserMessage getUserMessageToSummarize(String text) {
        String userMessageTemplate = """
                [요약할 원문]
                <<<
                {text}
                >>>
                
                [요약 결과]
                """;
        return new UserMessage(new PromptTemplate(userMessageTemplate).render(Map.of("text", text)));
    }

    @LogExecutionTime
    public String generateSlug(String title, String text) {
        SystemMessage systemMessage = getSystemMessageToSlug();
        UserMessage userMessage = getUserMessageToSlug(title, text);

        return chatClient.prompt()
                .messages(systemMessage, userMessage)
                .call()
                .content();
    }

    private SystemMessage getSystemMessageToSlug() {
        String msg = """
            당신은 URL 슬러그(slug)를 생성하는 SEO 전문가입니다.
            제공된 제목과 원문을 기반으로 검색 엔진 최적화(SEO)에 유리한 슬러그를 생성해야 합니다.

            [최종 출력 조건]
            - 슬러그는 반드시 영어 소문자로만 구성되어야 합니다.
            - 단어와 단어 사이는 하이픈(-)으로 연결합니다.
            - 불필요한 관사, 전치사, 대명사(stop words)는 제거하여 간결하게 만듭니다.
            - 제목과 원문의 핵심 키워드를 포함하여 주제를 잘 나타내야 합니다.
            - 특수문자나 공백은 포함하지 않습니다.
            - 최종 출력은 오직 생성된 슬러그여야 합니다. 다른 설명이나 접두사는 붙이지 마세요.
            - 예시: "스프링 부트 3.0의 새로운 기능" -> "spring-boot-3-new-features"
            """;
        return new SystemMessage(msg);
    }

    private UserMessage getUserMessageToSlug(String title, String text) {
        String userMessageTemplate = """
                [대상 제목]
                <<<
                {title}
                >>>
                
                [대상 원문]
                <<<
                {text}
                >>>
                
                [Slug 생성 결과]
                """;
        return new UserMessage(new PromptTemplate(userMessageTemplate).render(Map.of("title", title, "text", text)));
    }
}


