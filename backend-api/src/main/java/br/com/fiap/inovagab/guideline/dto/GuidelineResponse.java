package br.com.fiap.inovagab.guideline.dto;

import java.time.Instant;
import java.util.List;

import br.com.fiap.inovagab.guideline.document.StrategicGuidelineDocument;
import br.com.fiap.inovagab.guideline.model.GuidelineStatus;

public record GuidelineResponse(
        String id,
        String title,
        String description,
        String category,
        String campaign,
        GuidelineStatus status,
        Instant validFrom,
        Instant validUntil,
        Integer version,
        String createdBy,
        String updatedBy,
        Instant createdAt,
        Instant updatedAt,
        List<GuidelineVersionResponse> history
) {

    public static GuidelineResponse from(StrategicGuidelineDocument guideline) {
        List<GuidelineVersionResponse> history = guideline.getHistory() == null
                ? List.of()
                : guideline.getHistory().stream()
                        .map(GuidelineVersionResponse::from)
                        .toList();

        return new GuidelineResponse(
                guideline.getId(),
                guideline.getTitle(),
                guideline.getDescription(),
                guideline.getCategory(),
                guideline.getCampaign(),
                guideline.getStatus(),
                guideline.getValidFrom(),
                guideline.getValidUntil(),
                guideline.getVersion(),
                guideline.getCreatedBy(),
                guideline.getUpdatedBy(),
                guideline.getCreatedAt(),
                guideline.getUpdatedAt(),
                history
        );
    }
}
