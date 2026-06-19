package taskmanager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaService {

    private final OllamaConfig ollamaConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Call Ollama Mistral to parse natural language task description
     * and return a structured Task object
     */
    public Task suggestTaskFromDescription(String description) {
        try {
            String prompt = buildTaskSuggestionPrompt(description);
            String response = callOllama(prompt);
            return parseTaskFromResponse(response, description);
        } catch (Exception e) {
            log.error("Error calling Ollama for task suggestion", e);
            // Fallback: create basic task from description
            return Task.builder()
                    .title(description.length() > 100 ? description.substring(0, 100) : description)
                    .description(description)
                    .status(Task.Status.TODO)
                    .priority(Task.Priority.MEDIUM)
                    .build();
        }
    }

    private String buildTaskSuggestionPrompt(String userDescription) {
        return String.format("""
                You are a task management AI. Parse this user description and extract task details.
                Return ONLY a JSON object (no markdown, no code blocks) with these fields:
                - title (string, max 100 chars, required)
                - description (string, optional)
                - priority (LOW, MEDIUM, or HIGH, default MEDIUM)
                - dueDate (ISO date YYYY-MM-DD, optional)
                
                User description: "%s"
                
                Return only valid JSON:""", userDescription);
    }

    private String callOllama(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", ollamaConfig.getModel());
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            String url = ollamaConfig.getBaseUrl() + "/api/generate";

            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            if (response != null && response.containsKey("response")) {
                return response.get("response").toString();
            }
            return "{}";
        } catch (Exception e) {
            log.error("Error communicating with Ollama", e);
            return "{}";
        }
    }

    private Task parseTaskFromResponse(String jsonResponse, String userDescription) {
        try {
            String cleaned = stripMarkdownCodeFences(jsonResponse);
            Map<String, Object> data = parseTaskMap(cleaned);

            String title = extractString(data, "title");
            if (title == null || title.isBlank()) {
                title = truncateTitle(userDescription);
            }

            String description = extractString(data, "description");
            if (description == null || description.isBlank()) {
                description = userDescription;
            }

            Task.Priority priority = parsePriority(extractString(data, "priority"));

            LocalDate dueDate = parseDueDate(extractString(data, "dueDate"));

            return Task.builder()
                    .title(title)
                    .description(description)
                    .priority(priority)
                    .dueDate(dueDate)
                    .status(Task.Status.TODO)
                    .build();

        } catch (Exception e) {
            log.error("Error parsing task from Ollama response", e);
            // Return basic task as fallback
            return Task.builder()
                    .title(userDescription.length() > 100 ? userDescription.substring(0, 100) : userDescription)
                    .description(userDescription)
                    .status(Task.Status.TODO)
                    .priority(Task.Priority.MEDIUM)
                    .build();
        }
    }

    private String stripMarkdownCodeFences(String jsonResponse) {
        String cleaned = jsonResponse == null ? "" : jsonResponse.trim();
        cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "");
        cleaned = cleaned.replaceFirst("\\s*```$", "");
        return cleaned.trim();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseTaskMap(String cleanedJson) throws Exception {
        Map<String, Object> data = objectMapper.readValue(cleanedJson, Map.class);
        if (data != null) {
            return data;
        }

        log.debug("Injected ObjectMapper returned null; retrying with a local mapper");
        data = new ObjectMapper().readValue(cleanedJson, Map.class);
        if (data == null) {
            throw new IllegalStateException("Parsed task response was null");
        }
        return data;
    }

    private String extractString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value == null ? null : value.toString();
    }

    private String truncateTitle(String text) {
        if (text == null) {
            return "";
        }
        return text.length() > 100 ? text.substring(0, 100) : text;
    }

    private Task.Priority parsePriority(String priorityValue) {
        if (priorityValue == null || priorityValue.isBlank()) {
            return Task.Priority.MEDIUM;
        }

        try {
            return Task.Priority.valueOf(priorityValue.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.warn("Could not parse priority: {}", priorityValue);
            return Task.Priority.MEDIUM;
        }
    }

    private LocalDate parseDueDate(String dueDateValue) {
        if (dueDateValue == null || dueDateValue.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(dueDateValue.trim(), DateTimeFormatter.ISO_DATE);
        } catch (Exception e) {
            log.warn("Could not parse due date: {}", dueDateValue);
            return null;
        }
    }
}
