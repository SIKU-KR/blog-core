package park.bumsiku.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import park.bumsiku.repository.ImageRepository;
import park.bumsiku.service.LlmService;

/**
 * Base class for all integration tests that require Spring Boot test context.
 * Provides common configuration and mocks for external dependencies only.
 * <p>
 * Integration tests use real services but mock external dependencies like:
 * - External APIs (ImageRepository for AWS S3)
 * - AI services (LlmService for OpenAI)
 */
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
public abstract class AbstractTestSupport {

    // Mock external dependencies only
    @MockitoBean
    protected ImageRepository imageRepository;

    @MockitoBean
    protected LlmService llmService;

    @Autowired(required = false)
    protected MockMvc mockMvc;

    @Autowired(required = false)
    protected ObjectMapper objectMapper;
}
