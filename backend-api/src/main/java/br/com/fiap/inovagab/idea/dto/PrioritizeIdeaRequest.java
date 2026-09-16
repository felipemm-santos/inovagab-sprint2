package br.com.fiap.inovagab.idea.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import br.com.fiap.inovagab.idea.model.IdeaPriority;

public record PrioritizeIdeaRequest(
        @NotNull IdeaPriority priority,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0")
        @Digits(integer = 3, fraction = 2) BigDecimal managerScore
) {
}
