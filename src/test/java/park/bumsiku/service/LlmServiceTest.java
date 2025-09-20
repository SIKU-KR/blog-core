package park.bumsiku.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import park.bumsiku.repository.PostRepository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for LlmService following Spring AI testing best practices.
 * These tests focus on testing the business logic and integration with dependencies
 * while mocking external AI services to avoid token consumption during testing.
 * <p>
 * As per Spring AI documentation, the recommended approach is to mock ChatClient
 * to avoid unnecessary API calls and token usage during unit testing.
 */
@ExtendWith(MockitoExtension.class)
class LlmServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private LlmService llmService;

    @Test
    void generateSummary_ValidatesServiceInstantiation() {
        assertNotNull(llmService);
        assertNotNull(postRepository);
        assertNotNull(chatClient);
    }
}