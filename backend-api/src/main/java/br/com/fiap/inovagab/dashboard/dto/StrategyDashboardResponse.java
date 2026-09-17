package br.com.fiap.inovagab.dashboard.dto;

import br.com.fiap.inovagab.guideline.model.GuidelineStatus;

public record StrategyDashboardResponse(
        String guidelineId,
        String guidelineTitle,
        String guidelineCategory,
        GuidelineStatus guidelineStatus,
        DashboardSummaryResponse summary
) {
}
