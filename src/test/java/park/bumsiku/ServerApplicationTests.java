package park.bumsiku;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import park.bumsiku.config.AbstractTestSupport;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ServerApplicationTests extends AbstractTestSupport {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertNotNull(context);
    }

}
