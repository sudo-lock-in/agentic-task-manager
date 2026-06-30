package taskmanager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestTaskRequest {
    // Handles user input box
    @NotBlank(message = "description is required")
    private String description;
}
