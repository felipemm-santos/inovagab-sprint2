package br.com.fiap.inovagab.idea.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateIdeaRequest(
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 4000) String description,
        @NotBlank @Size(max = 80) String category,
        @NotBlank String strategicGuidelineId
) {
}
