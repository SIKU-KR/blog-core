package park.bumsiku.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class Monitoring {

    /**
     * 프로메테우스 설정을 위해 Node Exposure 옵션을 설정합니다.
     * @return properties 객체
     */
    @Bean
    public Map<String, Object> monitoringProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("management.endpoints.web.exposure.include", "*");
        return properties;
    }

}
