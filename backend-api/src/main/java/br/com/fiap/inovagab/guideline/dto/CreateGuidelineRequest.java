package br.com.fiap.inovagab.guideline.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import br.com.fiap.inovagab.guideline.model.GuidelineStatus;

public record CreateGuidelineRequest(
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 4000) String description,
        @NotBlank @Size(max = 80) String category,
        @NotBlank @Size(max = 120) String campaign,
        @NotNull GuidelineStatus status,
        Instant validFrom,
        Instant validUntil
) {
}
