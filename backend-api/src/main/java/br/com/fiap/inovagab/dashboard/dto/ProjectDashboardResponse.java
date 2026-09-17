package br.com.fiap.inovagab.dashboard.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import br.com.fiap.inovagab.project.model.ProjectStatus;

public record ProjectDashboardResponse(
        String projectId,
        String name,
        String strategicGuidelineId,
        String managerId,
        ProjectStatus status,
        String stage,
        LocalDate startDate,
        LocalDate expectedEndDate,
        Instant completedAt,
        BigDecimal investment,
        BigDecimal financialReturn,
        BigDecimal profit,
        BigDecimal roiPercentage,
        String roiUnavailableReason,
        BigDecimal costReduction,
        BigDecimal productivityGain,
        Long plannedDurationDays,
        Long elapsedDurationDays,
        Long remainingDays,
        boolean overdue,
        Instant generatedAt
) {
}
