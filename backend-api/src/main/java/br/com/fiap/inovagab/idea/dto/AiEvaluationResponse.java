package br.com.fiap.inovagab.idea.dto;

import java.util.List;

import br.com.fiap.inovagab.idea.model.IdeaPriority;

public record AiEvaluationResponse(
        int score,
        IdeaPriority priority,
        List<String> strategyIds,
        String reason
) {
}
