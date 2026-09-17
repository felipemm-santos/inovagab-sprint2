package br.com.fiap.inovagab.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import br.com.fiap.inovagab.MongoIntegrationTestSupport;
import br.com.fiap.inovagab.audit.model.AuditEventType;
import br.com.fiap.inovagab.audit.repository.AuditEventRepository;
import br.com.fiap.inovagab.guideline.document.StrategicGuidelineDocument;
import br.com.fiap.inovagab.guideline.model.GuidelineStatus;
import br.com.fiap.inovagab.guideline.repository.StrategicGuidelineRepository;
import br.com.fiap.inovagab.idea.document.IdeaDocument;
import br.com.fiap.inovagab.idea.model.IdeaStatus;
import br.com.fiap.inovagab.idea.repository.IdeaRepository;
import br.com.fiap.inovagab.project.repository.ProjectRepository;
import br.com.fiap.inovagab.user.model.UserRole;

@SpringBootTest
@AutoConfigureMockMvc
class BusinessCrudAuthorizationIntegrationTest
        extends MongoIntegrationTestSupport {

    private static final String OPERATOR_ID = "64b000000000000000000001";
    private static final String OTHER_OPERATOR_ID = "64b000000000000000000002";
    private static final String MANAGER_ID = "64b000000000000000000003";
    private static final String LEADER_ID = "64b000000000000000000004";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StrategicGuidelineRepository guidelineRepository;

    @Autowired
    private IdeaRepository ideaRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @BeforeEach
    void cleanDatabase() {
        auditEventRepository.deleteAll();
        projectRepository.deleteAll();
        ideaRepository.deleteAll();
        guidelineRepository.deleteAll();
    }

    @Test
    void leaderShouldCompleteGuidelineCrudAndManagerShouldOnlyReadActive()
            throws Exception {
        mockMvc.perform(post("/v1/guidelines")
                        .with(as(UserRole.LIDER, LEADER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(guidelineBody(
                                "Eficiência operacional",
                                "ACTIVE"
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        StrategicGuidelineDocument created = guidelineRepository
                .findAll()
                .getFirst();

        mockMvc.perform(get("/v1/guidelines/{id}", created.getId())
                        .with(as(UserRole.GESTOR, MANAGER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()));

        mockMvc.perform(put("/v1/guidelines/{id}", created.getId())
                        .with(as(UserRole.LIDER, LEADER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(guidelineBody(
                                "Eficiência e produtividade",
                                "ACTIVE"
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.history[0].version").value(1));

        mockMvc.perform(delete("/v1/guidelines/{id}", created.getId())
                        .with(as(UserRole.LIDER, LEADER_ID)))
                .andExpect(status().isNoContent());

        StrategicGuidelineDocument deleted = guidelineRepository
                .findById(created.getId())
                .orElseThrow();
        assertThat(deleted.getDeletedAt()).isNotNull();
        assertThat(deleted.getStatus()).isEqualTo(GuidelineStatus.ARCHIVED);

        mockMvc.perform(get("/v1/guidelines/{id}", created.getId())
                        .with(as(UserRole.GESTOR, MANAGER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GUIDELINE_NOT_FOUND"));
    }

    @Test
    void onlyLeaderShouldWriteGuidelines() throws Exception {
        String body = guidelineBody("Diretriz bloqueada", "ACTIVE");

        mockMvc.perform(post("/v1/guidelines")
                        .with(as(UserRole.GESTOR, MANAGER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        mockMvc.perform(post("/v1/guidelines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code")
                        .value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void invalidGuidelinePeriodShouldReturnStandardizedBadRequest()
            throws Exception {
        mockMvc.perform(post("/v1/guidelines")
                        .with(as(UserRole.LIDER, LEADER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Diretriz",
                                  "description": "Descrição válida",
                                  "category": "Operações",
                                  "campaign": "2026",
                                  "status": "ACTIVE",
                                  "validFrom": "2026-12-31T00:00:00Z",
                                  "validUntil": "2026-01-01T00:00:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_VALIDITY_PERIOD"))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void operatorShouldCompleteOwnedIdeaCrudAndNotAccessAnotherAuthorsIdea()
            throws Exception {
        StrategicGuidelineDocument guideline = activeGuideline();

        mockMvc.perform(post("/v1/ideas")
                        .with(as(UserRole.OPERADOR, OPERATOR_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ideaBody(
                                guideline.getId(),
                                "Reduzir tempo de embarque"
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorId").value(OPERATOR_ID))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

        IdeaDocument idea = ideaRepository.findAll().getFirst();

        mockMvc.perform(get("/v1/ideas/mine")
                        .with(as(UserRole.OPERADOR, OPERATOR_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(idea.getId()));

        mockMvc.perform(put("/v1/ideas/{id}", idea.getId())
                        .with(as(UserRole.OPERADOR, OPERATOR_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ideaBody(
                                guideline.getId(),
                                "Embarque digital"
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Embarque digital"));

        mockMvc.perform(get("/v1/ideas/{id}", idea.getId())
                        .with(as(UserRole.OPERADOR, OTHER_OPERATOR_ID)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("IDEA_ACCESS_DENIED"));

        mockMvc.perform(delete("/v1/ideas/{id}", idea.getId())
                        .with(as(UserRole.OPERADOR, OTHER_OPERATOR_ID)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/v1/ideas/{id}", idea.getId())
                        .with(as(UserRole.OPERADOR, OPERATOR_ID)))
                .andExpect(status().isNoContent());

        assertThat(ideaRepository.existsById(idea.getId())).isFalse();
    }

    @Test
    void managerShouldPrioritizeApproveAndCreateOnlyOneProject()
            throws Exception {
        StrategicGuidelineDocument guideline = activeGuideline();
        IdeaDocument idea = ideaRepository.save(IdeaDocument.builder()
                .title("Otimizar manutenção")
                .description("Prever manutenção com dados operacionais")
                .category("Operações")
                .authorId(OPERATOR_ID)
                .strategicGuidelineId(guideline.getId())
                .status(IdeaStatus.SUBMITTED)
                .build());

        mockMvc.perform(patch("/v1/ideas/{id}/priority", idea.getId())
                        .with(as(UserRole.GESTOR, MANAGER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "priority": "HIGH",
                                  "managerScore": 92.5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"))
                .andExpect(jsonPath("$.priority").value("HIGH"));

        String decision = "{\"comment\":\"Alinhada à estratégia e viável.\"}";
        mockMvc.perform(post("/v1/ideas/{id}/approve", idea.getId())
                        .with(as(UserRole.GESTOR, MANAGER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(decision))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idea.status").value("APPROVED"))
                .andExpect(jsonPath("$.projectCreated").value(true))
                .andExpect(jsonPath("$.project.sourceIdeaId")
                        .value(idea.getId()));

        mockMvc.perform(post("/v1/ideas/{id}/approve", idea.getId())
                        .with(as(UserRole.GESTOR, MANAGER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(decision))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectCreated").value(false));

        assertThat(projectRepository.count()).isEqualTo(1);

        mockMvc.perform(post("/v1/ideas/{id}/approve", idea.getId())
                        .with(as(UserRole.OPERADOR, OPERATOR_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(decision))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/v1/ideas/{id}", idea.getId())
                        .with(as(UserRole.OPERADOR, OPERATOR_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ideaBody(guideline.getId(), "Alteração tardia")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_IDEA_STATUS"));
    }

    @Test
    void managerShouldManageProjectsWhileLeaderHasReadOnlyAccess()
            throws Exception {
        StrategicGuidelineDocument guideline = activeGuideline();

        mockMvc.perform(post("/v1/projects")
                        .with(as(UserRole.GESTOR, MANAGER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectBody(
                                guideline.getId(),
                                "IN_PROGRESS",
                                "100.00",
                                "200.00"
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profit").value(100.0))
                .andExpect(jsonPath("$.roiPercentage").value(100.0));

        String projectId = projectRepository.findAll().getFirst().getId();

        mockMvc.perform(get("/v1/projects/{id}", projectId)
                        .with(as(UserRole.LIDER, LEADER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId));

        mockMvc.perform(put("/v1/projects/{id}", projectId)
                        .with(as(UserRole.LIDER, LEADER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectBody(
                                guideline.getId(),
                                "COMPLETED",
                                "100.00",
                                "250.00"
                        )))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/v1/projects/{id}", projectId)
                        .with(as(UserRole.GESTOR, MANAGER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectBody(
                                guideline.getId(),
                                "COMPLETED",
                                "100.00",
                                "250.00"
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").isNotEmpty());

        mockMvc.perform(post("/v1/projects")
                        .with(as(UserRole.GESTOR, MANAGER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectBody(
                                guideline.getId(),
                                "PLANNED",
                                "-1.00",
                                "0.00"
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        mockMvc.perform(get("/v1/projects")
                        .with(as(UserRole.OPERADOR, OPERATOR_ID)))
                .andExpect(status().isForbidden());
    }

    @Test
    void projectShouldRejectExpiredGuidelineOnCreateAndStrategyChange()
            throws Exception {

        StrategicGuidelineDocument activeGuideline = activeGuideline();

        StrategicGuidelineDocument expiredGuideline = expiredGuideline();

        mockMvc.perform(post("/v1/projects")
                        .with(as(UserRole.GESTOR, MANAGER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectBody(
                                expiredGuideline.getId(),
                                "PLANNED",
                                "100.00",
                                "0.00"
                        )))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("GUIDELINE_NOT_ACTIVE"));

        mockMvc.perform(post("/v1/projects")
                        .with(as(UserRole.GESTOR, MANAGER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectBody(
                                activeGuideline.getId(),
                                "PLANNED",
                                "100.00",
                                "0.00"
                        )))
                .andExpect(status().isCreated());

        String projectId = projectRepository.findAll().getFirst().getId();
        mockMvc.perform(put("/v1/projects/{id}", projectId)
                        .with(as(UserRole.GESTOR, MANAGER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectBody(
                                expiredGuideline.getId(),
                                "IN_PROGRESS",
                                "100.00",
                                "0.00"
                        )))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("GUIDELINE_NOT_ACTIVE"));
    }

    @Test
    void onlyManagerShouldSoftDeleteProjectAndRecordAudit() throws Exception {
        StrategicGuidelineDocument guideline = activeGuideline();

        mockMvc.perform(post("/v1/projects")
                        .with(as(UserRole.GESTOR, MANAGER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectBody(
                                guideline.getId(),
                                "PLANNED",
                                "100.00",
                                "0.00"
                        )))
                .andExpect(status().isCreated());

        String projectId = projectRepository.findAll().getFirst().getId();

        mockMvc.perform(delete("/v1/projects/{id}", projectId)
                        .with(as(UserRole.LIDER, LEADER_ID)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        mockMvc.perform(delete("/v1/projects/{id}", projectId)
                        .with(as(UserRole.OPERADOR, OPERATOR_ID)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        mockMvc.perform(delete("/v1/projects/{id}", projectId)
                        .with(as(UserRole.GESTOR, MANAGER_ID)))
                .andExpect(status().isNoContent());

        var deleted = projectRepository.findById(projectId).orElseThrow();
        assertThat(deleted.getDeletedAt()).isNotNull();
        assertThat(deleted.getDeletedBy()).isEqualTo(MANAGER_ID);
        assertThat(auditEventRepository.findAll())
                .anySatisfy(event -> {
                    assertThat(event.getEventType())
                            .isEqualTo(AuditEventType.PROJECT_DELETED);
                    assertThat(event.getEntityId()).isEqualTo(projectId);
                    assertThat(event.getActorId()).isEqualTo(MANAGER_ID);
                });

        mockMvc.perform(get("/v1/projects/{id}", projectId)
                        .with(as(UserRole.LIDER, LEADER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROJECT_NOT_FOUND"));

        mockMvc.perform(get("/v1/projects")
                        .with(as(UserRole.GESTOR, MANAGER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    private StrategicGuidelineDocument activeGuideline() {
        return guidelineRepository.save(StrategicGuidelineDocument.builder()
                .title("Excelência operacional")
                .description("Aumentar eficiência com segurança")
                .category("Operações")
                .campaign("Plano 2026")
                .status(GuidelineStatus.ACTIVE)
                .validFrom(Instant.now().minusSeconds(3600))
                .validUntil(Instant.now().plusSeconds(86_400))
                .version(1)
                .createdBy(LEADER_ID)
                .updatedBy(LEADER_ID)
                .build());
    }

    private StrategicGuidelineDocument expiredGuideline() {
        return guidelineRepository.save(StrategicGuidelineDocument.builder()
                .title("Diretriz vencida")
                .description("Diretriz fora do período de vigência")
                .category("Operações")
                .campaign("Plano 2025")
                .status(GuidelineStatus.ACTIVE)
                .validFrom(Instant.now().minusSeconds(7200))
                .validUntil(Instant.now().minusSeconds(3600))
                .version(1)
                .createdBy(LEADER_ID)
                .updatedBy(LEADER_ID)
                .build());
    }

    private RequestPostProcessor as(UserRole role, String subject) {
        return jwt()
                .jwt(token -> token
                        .subject(subject)
                        .claim("roles", List.of(role.name())))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    private String guidelineBody(String title, String status) {
        return """
                {
                  "title": "%s",
                  "description": "Descrição estratégica detalhada",
                  "category": "Operações",
                  "campaign": "Plano 2026",
                  "status": "%s"
                }
                """.formatted(title, status);
    }

    private String ideaBody(String guidelineId, String title) {
        return """
                {
                  "title": "%s",
                  "description": "Descrição completa da oportunidade",
                  "category": "Operações",
                  "strategicGuidelineId": "%s"
                }
                """.formatted(title, guidelineId);
    }

    private String projectBody(
            String guidelineId,
            String status,
            String investment,
            String financialReturn
    ) {
        return """
                {
                  "strategicGuidelineId": "%s",
                  "name": "Projeto de eficiência",
                  "description": "Execução da iniciativa aprovada",
                  "status": "%s",
                  "stage": "Piloto",
                  "startDate": "2026-09-01",
                  "expectedEndDate": "2026-12-15",
                  "investment": %s,
                  "financialReturn": %s,
                  "costReduction": 25.00,
                  "productivityGain": 10.00
                }
                """.formatted(
                guidelineId,
                status,
                investment,
                financialReturn
        );
    }
}
