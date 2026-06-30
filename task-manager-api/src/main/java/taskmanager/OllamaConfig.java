package taskmanager;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ollama")
@Data
public class OllamaConfig {
    private String baseUrl = "http://localhost:11434"; // This is where ollama listens from
    private String model = "mistral";
}
