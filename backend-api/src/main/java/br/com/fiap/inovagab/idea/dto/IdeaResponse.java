package br.com.fiap.inovagab.idea.dto;

import java.math.BigDecimal;
import java.time.Instant;

import br.com.fiap.inovagab.idea.document.IdeaDocument;
import br.com.fiap.inovagab.idea.model.IdeaPriority;
import br.com.fiap.inovagab.idea.model.IdeaStatus;

public record IdeaResponse(
        String id,
        String title,
        String description,
        String category,
        String authorId,
        String strategicGuidelineId,
        IdeaStatus status,
        IdeaPriority priority,
        BigDecimal managerScore,
        BigDecimal aiScore,
        String aiJustification,
        String managerComment,
        String evaluatedBy,
        Instant evaluatedAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static IdeaResponse from(IdeaDocument idea) {
        return new IdeaResponse(
                idea.getId(),
                idea.getTitle(),
                idea.getDescription(),
                idea.getCategory(),
                idea.getAuthorId(),
                idea.getStrategicGuidelineId(),
                idea.getStatus(),
                idea.getPriority(),
                idea.getManagerScore(),
                idea.getAiScore(),
                idea.getAiJustification(),
                idea.getManagerComment(),
                idea.getEvaluatedBy(),
                idea.getEvaluatedAt(),
                idea.getCreatedAt(),
                idea.getUpdatedAt()
        );
    }
}
