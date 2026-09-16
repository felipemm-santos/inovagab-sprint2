package br.com.fiap.inovagab.guideline.dto;

import java.time.Instant;

import br.com.fiap.inovagab.guideline.document.GuidelineVersionSnapshot;
import br.com.fiap.inovagab.guideline.model.GuidelineStatus;

public record GuidelineVersionResponse(
        Integer version,
        String title,
        String description,
        String category,
        String campaign,
        GuidelineStatus status,
        Instant validFrom,
        Instant validUntil,
        String changedBy,
        Instant changedAt
) {

    public static GuidelineVersionResponse from(GuidelineVersionSnapshot snapshot) {
        return new GuidelineVersionResponse(
                snapshot.getVersion(),
                snapshot.getTitle(),
                snapshot.getDescription(),
                snapshot.getCategory(),
                snapshot.getCampaign(),
                snapshot.getStatus(),
                snapshot.getValidFrom(),
                snapshot.getValidUntil(),
                snapshot.getChangedBy(),
                snapshot.getChangedAt()
        );
    }
}
