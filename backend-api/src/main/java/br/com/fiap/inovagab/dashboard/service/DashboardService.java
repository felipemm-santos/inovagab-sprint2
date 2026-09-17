package br.com.fiap.inovagab.dashboard.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import br.com.fiap.inovagab.dashboard.dto.DashboardSummaryResponse;
import br.com.fiap.inovagab.dashboard.dto.ProjectDashboardResponse;
import br.com.fiap.inovagab.dashboard.dto.StrategyDashboardResponse;
import br.com.fiap.inovagab.guideline.document.StrategicGuidelineDocument;
import br.com.fiap.inovagab.guideline.service.GuidelineService;
import br.com.fiap.inovagab.project.document.ProjectDocument;
import br.com.fiap.inovagab.project.model.ProjectStatus;
import br.com.fiap.inovagab.project.repository.ProjectRepository;
import br.com.fiap.inovagab.shared.exception.ApiException;
import br.com.fiap.inovagab.shared.validation.MongoIdValidator;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int DECIMAL_SCALE = 2;
    private static final String ZERO_INVESTMENT_REASON =
            "ROI is unavailable when total investment is zero";

    private final ProjectRepository projectRepository;
    private final GuidelineService guidelineService;

    public DashboardSummaryResponse getSummary() {
        Instant generatedAt = Instant.now();
        return summarize(
                projectRepository.findAllByDeletedAtIsNullOrderByUpdatedAtDesc(),
                generatedAt,
                LocalDate.ofInstant(generatedAt, ZoneOffset.UTC)
        );
    }

    public ProjectDashboardResponse getProjectReport(String projectId) {
        MongoIdValidator.requireValid(projectId);
        ProjectDocument project = projectRepository
                .findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "PROJECT_NOT_FOUND",
                        "Project was not found"
                ));

        Instant generatedAt = Instant.now();
        LocalDate today = LocalDate.ofInstant(generatedAt, ZoneOffset.UTC);
        BigDecimal investment = valueOrZero(project.getInvestment());
        BigDecimal financialReturn = valueOrZero(project.getFinancialReturn());
        BigDecimal profit = financialReturn.subtract(investment);
        BigDecimal roi = calculateRoi(profit, investment);

        return new ProjectDashboardResponse(
                project.getId(),
                project.getName(),
                project.getStrategicGuidelineId(),
                project.getManagerId(),
                project.getStatus(),
                project.getStage(),
                project.getStartDate(),
                project.getExpectedEndDate(),
                project.getCompletedAt(),
                investment,
                financialReturn,
                profit,
                roi,
                roi == null
                        ? "ROI is unavailable when investment is zero"
                        : null,
                valueOrZero(project.getCostReduction()),
                valueOrZero(project.getProductivityGain()),
                plannedDurationDays(project),
                elapsedDurationDays(project, today),
                remainingDays(project, today),
                isOverdue(project, today),
                generatedAt
        );
    }

    public StrategyDashboardResponse getStrategyReport(String guidelineId) {
        StrategicGuidelineDocument guideline = guidelineService
                .requireExisting(guidelineId);
        Instant generatedAt = Instant.now();

        DashboardSummaryResponse summary = summarize(
                projectRepository
                        .findAllByStrategicGuidelineIdAndDeletedAtIsNull(
                                guidelineId
                        ),
                generatedAt,
                LocalDate.ofInstant(generatedAt, ZoneOffset.UTC)
        );

        return new StrategyDashboardResponse(
                guideline.getId(),
                guideline.getTitle(),
                guideline.getCategory(),
                guideline.getStatus(),
                summary
        );
    }

    private DashboardSummaryResponse summarize(
            List<ProjectDocument> projects,
            Instant generatedAt,
            LocalDate today
    ) {
        Map<ProjectStatus, Long> projectsByStatus = emptyStatusCounters();
        BigDecimal totalInvestment = BigDecimal.ZERO;
        BigDecimal totalFinancialReturn = BigDecimal.ZERO;
        BigDecimal totalCostReduction = BigDecimal.ZERO;
        BigDecimal totalProductivityGain = BigDecimal.ZERO;
        long overdueProjects = 0;

        for (ProjectDocument project : projects) {
            ProjectStatus status = project.getStatus();
            if (status != null) {
                projectsByStatus.compute(status, (key, count) -> count + 1);
            }
            totalInvestment = totalInvestment.add(
                    valueOrZero(project.getInvestment())
            );
            totalFinancialReturn = totalFinancialReturn.add(
                    valueOrZero(project.getFinancialReturn())
            );
            totalCostReduction = totalCostReduction.add(
                    valueOrZero(project.getCostReduction())
            );
            totalProductivityGain = totalProductivityGain.add(
                    valueOrZero(project.getProductivityGain())
            );
            if (isOverdue(project, today)) {
                overdueProjects++;
            }
        }

        BigDecimal totalProfit = totalFinancialReturn.subtract(totalInvestment);
        BigDecimal overallRoi = calculateRoi(totalProfit, totalInvestment);

        return new DashboardSummaryResponse(
                projects.size(),
                Map.copyOf(projectsByStatus),
                totalInvestment,
                totalFinancialReturn,
                totalProfit,
                overallRoi,
                overallRoi == null ? ZERO_INVESTMENT_REASON : null,
                totalCostReduction,
                average(totalProductivityGain, projects.size()),
                projectsByStatus.get(ProjectStatus.COMPLETED),
                projectsByStatus.get(ProjectStatus.IN_PROGRESS),
                overdueProjects,
                averagePlannedDurationDays(projects),
                averageActualDurationDays(projects),
                generatedAt
        );
    }

    private Map<ProjectStatus, Long> emptyStatusCounters() {
        Map<ProjectStatus, Long> counters = new EnumMap<>(ProjectStatus.class);
        for (ProjectStatus status : ProjectStatus.values()) {
            counters.put(status, 0L);
        }
        return counters;
    }

    private BigDecimal calculateRoi(
            BigDecimal profit,
            BigDecimal investment
    ) {
        if (investment.signum() == 0) {
            return null;
        }
        return profit
                .multiply(BigDecimal.valueOf(100))
                .divide(investment, DECIMAL_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal average(BigDecimal total, long quantity) {
        if (quantity == 0) {
            return BigDecimal.ZERO.setScale(DECIMAL_SCALE);
        }
        return total.divide(
                BigDecimal.valueOf(quantity),
                DECIMAL_SCALE,
                RoundingMode.HALF_UP
        );
    }

    private BigDecimal averagePlannedDurationDays(
            List<ProjectDocument> projects
    ) {
        List<Long> durations = projects.stream()
                .map(this::plannedDurationDays)
                .filter(value -> value != null)
                .toList();
        return averageDays(durations);
    }

    private BigDecimal averageActualDurationDays(
            List<ProjectDocument> projects
    ) {
        List<Long> durations = projects.stream()
                .filter(project -> project.getStatus() == ProjectStatus.COMPLETED)
                .map(this::actualDurationDays)
                .filter(value -> value != null)
                .toList();
        return averageDays(durations);
    }

    private BigDecimal averageDays(List<Long> durations) {
        if (durations.isEmpty()) {
            return null;
        }
        long totalDays = durations.stream().mapToLong(Long::longValue).sum();
        return BigDecimal.valueOf(totalDays).divide(
                BigDecimal.valueOf(durations.size()),
                DECIMAL_SCALE,
                RoundingMode.HALF_UP
        );
    }

    private Long plannedDurationDays(ProjectDocument project) {
        if (project.getStartDate() == null
                || project.getExpectedEndDate() == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(
                project.getStartDate(),
                project.getExpectedEndDate()
        );
    }

    private Long actualDurationDays(ProjectDocument project) {
        if (project.getStartDate() == null || project.getCompletedAt() == null) {
            return null;
        }
        LocalDate completionDate = LocalDate.ofInstant(
                project.getCompletedAt(),
                ZoneOffset.UTC
        );
        return Math.max(
                0,
                ChronoUnit.DAYS.between(project.getStartDate(), completionDate)
        );
    }

    private Long elapsedDurationDays(
            ProjectDocument project,
            LocalDate today
    ) {
        if (project.getStartDate() == null) {
            return null;
        }
        LocalDate endDate = project.getCompletedAt() == null
                ? today
                : LocalDate.ofInstant(project.getCompletedAt(), ZoneOffset.UTC);
        return Math.max(
                0,
                ChronoUnit.DAYS.between(project.getStartDate(), endDate)
        );
    }

    private Long remainingDays(ProjectDocument project, LocalDate today) {
        if (project.getExpectedEndDate() == null
                || project.getStatus() == ProjectStatus.COMPLETED
                || project.getStatus() == ProjectStatus.CANCELLED) {
            return null;
        }
        return ChronoUnit.DAYS.between(today, project.getExpectedEndDate());
    }

    private boolean isOverdue(ProjectDocument project, LocalDate today) {
        return project.getExpectedEndDate() != null
                && project.getExpectedEndDate().isBefore(today)
                && project.getStatus() != ProjectStatus.COMPLETED
                && project.getStatus() != ProjectStatus.CANCELLED;
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
