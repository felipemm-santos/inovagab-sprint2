package br.com.fiap.inovagab.dashboard.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import br.com.fiap.inovagab.project.model.ProjectStatus;

public record DashboardSummaryResponse(
        long totalProjects,
        Map<ProjectStatus, Long> projectsByStatus,
        BigDecimal totalInvestment,
        BigDecimal totalFinancialReturn,
        BigDecimal totalProfit,
        BigDecimal overallRoiPercentage,
        String roiUnavailableReason,
        BigDecimal totalCostReduction,
        BigDecimal averageProductivityGain,
        long completedProjects,
        long inProgressProjects,
        long overdueProjects,
        BigDecimal averagePlannedDurationDays,
        BigDecimal averageActualDurationDays,
        Instant generatedAt
) {
}
