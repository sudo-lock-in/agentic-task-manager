package com.taskmanager.service;

import com.taskmanager.config.OllamaConfig;
import com.taskmanager.model.Task;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@DisplayName("OllamaService Unit Tests")
class OllamaServiceTest {

    @Mock
    private OllamaConfig ollamaConfig;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OllamaService ollamaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(ollamaConfig.getBaseUrl()).thenReturn("http://localhost:11434");
        when(ollamaConfig.getModel()).thenReturn("mistral");
    }

    @Test
    @DisplayName("suggestTaskFromDescription should parse Ollama response")
    void testSuggestTaskFromDescription_Success() throws Exception {
        String description = "remind me to submit the quarterly report before Friday";
        
        Map<String, Object> ollamaResponse = new HashMap<>();
        ollamaResponse.put("response", """
                {
                  "title": "Submit quarterly report",
                  "description": "reminder to submit the quarterly report",
                  "priority": "HIGH",
                  "dueDate": "2026-05-02"
                }
                """);

        when(restTemplate.postForObject(
                contains("/api/generate"),
                any(),
                eq(Map.class)))
                .thenReturn(ollamaResponse);

        Task result = ollamaService.suggestTaskFromDescription(description);

        assertNotNull(result);
        assertEquals("Submit quarterly report", result.getTitle());
        assertEquals(Task.Priority.HIGH, result.getPriority());
        assertEquals(Task.Status.TODO, result.getStatus());
    }

    @Test
    @DisplayName("suggestTaskFromDescription should handle empty description")
    void testSuggestTaskFromDescription_EmptyDescription() {
        String description = "   ";
        
        Task result = ollamaService.suggestTaskFromDescription(description);

        assertNotNull(result);
        // Should create basic task from description
    }

    @Test
    @DisplayName("suggestTaskFromDescription should set defaults when Ollama unavailable")
    void testSuggestTaskFromDescription_OllamaUnavailable() {
        String description = "test task";
        
        when(restTemplate.postForObject(
                anyString(),
                any(),
                eq(Map.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        Task result = ollamaService.suggestTaskFromDescription(description);

        assertNotNull(result);
        assertNotNull(result.getTitle());
        assertEquals(Task.Status.TODO, result.getStatus());
        assertEquals(Task.Priority.MEDIUM, result.getPriority());
    }

    @Test
    @DisplayName("suggestTaskFromDescription should handle invalid JSON response")
    void testSuggestTaskFromDescription_InvalidJson() throws Exception {
        String description = "test task";
        
        Map<String, Object> ollamaResponse = new HashMap<>();
        ollamaResponse.put("response", "invalid json {{{ ");

        when(restTemplate.postForObject(
                anyString(),
                any(),
                eq(Map.class)))
                .thenReturn(ollamaResponse);

        Task result = ollamaService.suggestTaskFromDescription(description);

        assertNotNull(result);
        // Should fallback gracefully
        assertEquals(Task.Status.TODO, result.getStatus());
    }

    @Test
    @DisplayName("suggestTaskFromDescription should handle missing fields")
    void testSuggestTaskFromDescription_MissingFields() throws Exception {
        String description = "test task";
        
        Map<String, Object> ollamaResponse = new HashMap<>();
        ollamaResponse.put("response", """
                {
                  "title": "Simple task"
                }
                """);

        when(restTemplate.postForObject(
                anyString(),
                any(),
                eq(Map.class)))
                .thenReturn(ollamaResponse);

        Task result = ollamaService.suggestTaskFromDescription(description);

        assertNotNull(result);
        assertEquals("Simple task", result.getTitle());
        // Should use defaults for missing fields
        assertEquals(Task.Priority.MEDIUM, result.getPriority());
        assertEquals(Task.Status.TODO, result.getStatus());
    }

    @Test
    @DisplayName("suggestTaskFromDescription should parse valid date")
    void testSuggestTaskFromDescription_WithValidDate() throws Exception {
        String description = "submit report on 2026-05-15";
        
        Map<String, Object> ollamaResponse = new HashMap<>();
        ollamaResponse.put("response", """
                {
                  "title": "Submit report",
                  "priority": "HIGH",
                  "dueDate": "2026-05-15"
                }
                """);

        when(restTemplate.postForObject(
                anyString(),
                any(),
                eq(Map.class)))
                .thenReturn(ollamaResponse);

        Task result = ollamaService.suggestTaskFromDescription(description);

        assertNotNull(result);
        assertEquals(LocalDate.of(2026, 5, 15), result.getDueDate());
    }

    @Test
    @DisplayName("suggestTaskFromDescription should handle markdown code blocks")
    void testSuggestTaskFromDescription_WithMarkdownCodeBlock() throws Exception {
        String description = "test task";
        
        Map<String, Object> ollamaResponse = new HashMap<>();
        ollamaResponse.put("response", """
                ```json
                {
                  "title": "Code block task",
                  "priority": "MEDIUM"
                }
                ```
                """);

        when(restTemplate.postForObject(
                anyString(),
                any(),
                eq(Map.class)))
                .thenReturn(ollamaResponse);

        Task result = ollamaService.suggestTaskFromDescription(description);

        assertNotNull(result);
        assertEquals("Code block task", result.getTitle());
    }

    @Test
    @DisplayName("suggestTaskFromDescription should truncate long titles")
    void testSuggestTaskFromDescription_LongTitle() {
        String description = "a very long description that should be truncated because it exceeds the maximum character limit for task titles in the system";
        
        Task result = ollamaService.suggestTaskFromDescription(description);

        assertNotNull(result);
        assertTrue(result.getTitle().length() <= 100);
    }
}
