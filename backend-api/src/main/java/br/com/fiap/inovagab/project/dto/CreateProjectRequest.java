package br.com.fiap.inovagab.project.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import br.com.fiap.inovagab.project.model.ProjectStatus;

public record CreateProjectRequest(
        @NotBlank String strategicGuidelineId,
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 4000) String description,
        @NotNull ProjectStatus status,
        @NotBlank @Size(max = 120) String stage,
        @NotNull LocalDate startDate,
        @NotNull LocalDate expectedEndDate,
        @NotNull @DecimalMin("0.0") @Digits(integer = 15, fraction = 2)
        BigDecimal investment,
        @NotNull @DecimalMin("0.0") @Digits(integer = 15, fraction = 2)
        BigDecimal financialReturn,
        @NotNull @DecimalMin("0.0") @Digits(integer = 15, fraction = 2)
        BigDecimal costReduction,
        @NotNull @DecimalMin("0.0") @Digits(integer = 8, fraction = 2)
        BigDecimal productivityGain
) {
}
