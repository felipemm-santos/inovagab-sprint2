package br.com.fiap.inovagab.idea.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IdeaDecisionRequest(
        @NotBlank @Size(max = 2000) String comment
) {
}
