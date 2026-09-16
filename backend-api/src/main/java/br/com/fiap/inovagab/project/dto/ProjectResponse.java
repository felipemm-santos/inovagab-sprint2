package br.com.fiap.inovagab.project.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;

import br.com.fiap.inovagab.project.document.ProjectDocument;
import br.com.fiap.inovagab.project.model.ProjectStatus;

public record ProjectResponse(
        String id,
        String sourceIdeaId,
        String strategicGuidelineId,
        String name,
        String description,
        String managerId,
        ProjectStatus status,
        String stage,
        LocalDate startDate,
        LocalDate expectedEndDate,
        Instant completedAt,
        BigDecimal investment,
        BigDecimal financialReturn,
        BigDecimal costReduction,
        BigDecimal productivityGain,
        BigDecimal profit,
        BigDecimal roiPercentage,
        String roiUnavailableReason,
        Instant createdAt,
        Instant updatedAt
) {

    public static ProjectResponse from(ProjectDocument project) {
        BigDecimal investment = valueOrZero(project.getInvestment());
        BigDecimal financialReturn = valueOrZero(project.getFinancialReturn());
        BigDecimal profit = financialReturn.subtract(investment);
        BigDecimal roi = null;
        String roiUnavailableReason = null;

        if (investment.signum() == 0) {
            roiUnavailableReason = "ROI is unavailable when investment is zero";
        } else {
            roi = profit
                    .multiply(BigDecimal.valueOf(100))
                    .divide(investment, 2, RoundingMode.HALF_UP);
        }

        return new ProjectResponse(
                project.getId(),
                project.getSourceIdeaId(),
                project.getStrategicGuidelineId(),
                project.getName(),
                project.getDescription(),
                project.getManagerId(),
                project.getStatus(),
                project.getStage(),
                project.getStartDate(),
                project.getExpectedEndDate(),
                project.getCompletedAt(),
                investment,
                financialReturn,
                valueOrZero(project.getCostReduction()),
                valueOrZero(project.getProductivityGain()),
                profit,
                roi,
                roiUnavailableReason,
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    private static BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
