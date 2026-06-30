package taskmanager;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/ollama")
@RequiredArgsConstructor
public class OllamaHealthController {

    private final OllamaService ollamaService;
    // Checks if ollama is running to provide any warnings
    @GetMapping("/health")
    public ResponseEntity<Map<String, Boolean>> health() {
        return ResponseEntity.ok(Map.of("running", ollamaService.isOllamaRunning()));
    }
}
