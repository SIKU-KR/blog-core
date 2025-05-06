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

/**
 * Base class for all tests that require Spring Boot test context.
 * Provides common configuration and dependencies.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
public abstract class AbstractTestSupport {

    @MockitoBean
    protected ImageRepository imageRepository;

    @Autowired(required = false)
    protected MockMvc mockMvc;

    @Autowired(required = false)
    protected ObjectMapper objectMapper;

    // 추가적으로 공통 Mock이 필요하다면 여기에 계속 추가
}
