package br.com.fiap.inovagab.dashboard;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import br.com.fiap.inovagab.MongoIntegrationTestSupport;
import br.com.fiap.inovagab.guideline.document.StrategicGuidelineDocument;
import br.com.fiap.inovagab.guideline.model.GuidelineStatus;
import br.com.fiap.inovagab.guideline.repository.StrategicGuidelineRepository;
import br.com.fiap.inovagab.project.document.ProjectDocument;
import br.com.fiap.inovagab.project.model.ProjectStatus;
import br.com.fiap.inovagab.project.repository.ProjectRepository;
import br.com.fiap.inovagab.user.model.UserRole;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardIntegrationTest extends MongoIntegrationTestSupport {

    private static final String LEADER_ID = "64b000000000000000000004";
    private static final String MANAGER_ID = "64b000000000000000000003";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private StrategicGuidelineRepository guidelineRepository;

    @BeforeEach
    void cleanDatabase() {
        projectRepository.deleteAll();
        guidelineRepository.deleteAll();
    }

    @Test
    void leaderShouldReceiveConsolidatedSummary() throws Exception {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        StrategicGuidelineDocument firstGuideline = saveGuideline(
                "Eficiência operacional"
        );
        StrategicGuidelineDocument secondGuideline = saveGuideline(
                "Experiência do cliente"
        );

        saveProject(
                firstGuideline.getId(),
                "Projeto concluído",
                ProjectStatus.COMPLETED,
                today.minusDays(20),
                today.minusDays(10),
                today.minusDays(12).atStartOfDay().toInstant(ZoneOffset.UTC),
                "100.00",
                "160.00",
                "10.00",
                "20.00"
        );
        saveProject(
                firstGuideline.getId(),
                "Projeto em andamento",
                ProjectStatus.IN_PROGRESS,
                today.minusDays(5),
                today.plusDays(5),
                null,
                "200.00",
                "250.00",
                "30.00",
                "40.00"
        );
        saveProject(
                secondGuideline.getId(),
                "Projeto atrasado",
                ProjectStatus.PAUSED,
                today.minusDays(20),
                today.minusDays(10),
                null,
                "0.00",
                "0.00",
                "5.00",
                "0.00"
        );

        mockMvc.perform(get("/v1/dashboard/summary")
                        .with(as(UserRole.LIDER, LEADER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(3))
                .andExpect(jsonPath("$.projectsByStatus.PLANNED").value(0))
                .andExpect(jsonPath("$.projectsByStatus.IN_PROGRESS").value(1))
                .andExpect(jsonPath("$.projectsByStatus.PAUSED").value(1))
                .andExpect(jsonPath("$.projectsByStatus.COMPLETED").value(1))
                .andExpect(jsonPath("$.projectsByStatus.CANCELLED").value(0))
                .andExpect(jsonPath("$.totalInvestment").value(300.0))
                .andExpect(jsonPath("$.totalFinancialReturn").value(410.0))
                .andExpect(jsonPath("$.totalProfit").value(110.0))
                .andExpect(jsonPath("$.overallRoiPercentage").value(36.67))
                .andExpect(jsonPath("$.roiUnavailableReason").doesNotExist())
                .andExpect(jsonPath("$.totalCostReduction").value(45.0))
                .andExpect(jsonPath("$.averageProductivityGain").value(20.0))
                .andExpect(jsonPath("$.completedProjects").value(1))
                .andExpect(jsonPath("$.inProgressProjects").value(1))
                .andExpect(jsonPath("$.overdueProjects").value(1))
                .andExpect(jsonPath("$.averagePlannedDurationDays").value(10.0))
                .andExpect(jsonPath("$.averageActualDurationDays").value(8.0))
                .andExpect(jsonPath("$.generatedAt").isNotEmpty());
    }

    @Test
    void leaderShouldReceiveReportsByStrategyAndProject() throws Exception {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        StrategicGuidelineDocument guideline = saveGuideline(
                "Eficiência operacional"
        );
        ProjectDocument project = saveProject(
                guideline.getId(),
                "Digitalização de inspeções",
                ProjectStatus.IN_PROGRESS,
                today.minusDays(5),
                today.plusDays(5),
                null,
                "200.00",
                "250.00",
                "30.00",
                "40.00"
        );

        mockMvc.perform(get(
                        "/v1/dashboard/strategies/{guidelineId}",
                        guideline.getId()
                ).with(as(UserRole.LIDER, LEADER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guidelineId").value(guideline.getId()))
                .andExpect(jsonPath("$.guidelineTitle")
                        .value("Eficiência operacional"))
                .andExpect(jsonPath("$.summary.totalProjects").value(1))
                .andExpect(jsonPath("$.summary.totalInvestment").value(200.0))
                .andExpect(jsonPath("$.summary.totalProfit").value(50.0))
                .andExpect(jsonPath("$.summary.overallRoiPercentage")
                        .value(25.0));

        mockMvc.perform(get(
                        "/v1/dashboard/projects/{projectId}",
                        project.getId()
                ).with(as(UserRole.LIDER, LEADER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(project.getId()))
                .andExpect(jsonPath("$.profit").value(50.0))
                .andExpect(jsonPath("$.roiPercentage").value(25.0))
                .andExpect(jsonPath("$.plannedDurationDays").value(10))
                .andExpect(jsonPath("$.elapsedDurationDays").value(5))
                .andExpect(jsonPath("$.remainingDays").value(5))
                .andExpect(jsonPath("$.overdue").value(false));
    }

    @Test
    void deletedProjectShouldBeAbsentFromEveryDashboardReport()
            throws Exception {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        StrategicGuidelineDocument guideline = saveGuideline(
                "Eficiência operacional"
        );
        ProjectDocument project = saveProject(
                guideline.getId(),
                "Projeto excluído",
                ProjectStatus.IN_PROGRESS,
                today.minusDays(5),
                today.plusDays(5),
                null,
                "200.00",
                "250.00",
                "30.00",
                "40.00"
        );
        project.setDeletedAt(Instant.now());
        project.setDeletedBy(MANAGER_ID);
        projectRepository.save(project);

        mockMvc.perform(get("/v1/dashboard/summary")
                        .with(as(UserRole.LIDER, LEADER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(0))
                .andExpect(jsonPath("$.totalInvestment").value(0));

        mockMvc.perform(get(
                        "/v1/dashboard/strategies/{guidelineId}",
                        guideline.getId()
                ).with(as(UserRole.LIDER, LEADER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.totalProjects").value(0));

        mockMvc.perform(get(
                        "/v1/dashboard/projects/{projectId}",
                        project.getId()
                ).with(as(UserRole.LIDER, LEADER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROJECT_NOT_FOUND"));
    }

    @Test
    void dashboardShouldBeRestrictedToLeader() throws Exception {
        mockMvc.perform(get("/v1/dashboard/summary")
                        .with(as(UserRole.GESTOR, MANAGER_ID)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        mockMvc.perform(get("/v1/dashboard/summary"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code")
                        .value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void reportsShouldReturnNotFoundForUnknownResources() throws Exception {
        String missingId = "64b000000000000000000099";

        mockMvc.perform(get(
                        "/v1/dashboard/projects/{projectId}",
                        missingId
                ).with(as(UserRole.LIDER, LEADER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROJECT_NOT_FOUND"));

        mockMvc.perform(get(
                        "/v1/dashboard/strategies/{guidelineId}",
                        missingId
                ).with(as(UserRole.LIDER, LEADER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GUIDELINE_NOT_FOUND"));
    }

    private StrategicGuidelineDocument saveGuideline(String title) {
        return guidelineRepository.save(StrategicGuidelineDocument.builder()
                .title(title)
                .description("Descrição da diretriz")
                .category("Operações")
                .campaign("Plano 2026")
                .status(GuidelineStatus.ACTIVE)
                .version(1)
                .createdBy(LEADER_ID)
                .updatedBy(LEADER_ID)
                .build());
    }

    private ProjectDocument saveProject(
            String guidelineId,
            String name,
            ProjectStatus status,
            LocalDate startDate,
            LocalDate expectedEndDate,
            Instant completedAt,
            String investment,
            String financialReturn,
            String costReduction,
            String productivityGain
    ) {
        return projectRepository.save(ProjectDocument.builder()
                .strategicGuidelineId(guidelineId)
                .name(name)
                .description("Descrição do projeto")
                .managerId(MANAGER_ID)
                .status(status)
                .stage("EXECUÇÃO")
                .startDate(startDate)
                .expectedEndDate(expectedEndDate)
                .completedAt(completedAt)
                .investment(new BigDecimal(investment))
                .financialReturn(new BigDecimal(financialReturn))
                .costReduction(new BigDecimal(costReduction))
                .productivityGain(new BigDecimal(productivityGain))
                .build());
    }

    private RequestPostProcessor as(UserRole role, String subject) {
        return jwt()
                .jwt(token -> token
                        .subject(subject)
                        .claim("roles", List.of(role.name())))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
