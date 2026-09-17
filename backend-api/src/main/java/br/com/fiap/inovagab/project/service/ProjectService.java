package br.com.fiap.inovagab.project.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import br.com.fiap.inovagab.audit.model.AuditEventType;
import br.com.fiap.inovagab.audit.service.BusinessAuditService;
import br.com.fiap.inovagab.guideline.service.GuidelineService;
import br.com.fiap.inovagab.idea.document.IdeaDocument;
import br.com.fiap.inovagab.project.document.ProjectDocument;
import br.com.fiap.inovagab.project.dto.CreateProjectRequest;
import br.com.fiap.inovagab.project.dto.ProjectResponse;
import br.com.fiap.inovagab.project.dto.UpdateProjectRequest;
import br.com.fiap.inovagab.project.model.ProjectStatus;
import br.com.fiap.inovagab.project.repository.ProjectRepository;
import br.com.fiap.inovagab.shared.exception.ApiException;
import br.com.fiap.inovagab.shared.validation.MongoIdValidator;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final GuidelineService guidelineService;
    private final BusinessAuditService auditService;

    public ProjectResponse create(CreateProjectRequest request, String managerId) {
        guidelineService.requireActiveAndEffective(
                request.strategicGuidelineId()
        );
        validateDates(request.startDate(), request.expectedEndDate());

        var project = ProjectDocument.builder()
                .strategicGuidelineId(request.strategicGuidelineId())
                .name(request.name().strip())
                .description(request.description().strip())
                .managerId(managerId)
                .status(request.status())
                .stage(request.stage().strip())
                .startDate(request.startDate())
                .expectedEndDate(request.expectedEndDate())
                .completedAt(completedAt(request.status(), null))
                .investment(request.investment())
                .financialReturn(request.financialReturn())
                .costReduction(request.costReduction())
                .productivityGain(request.productivityGain())
                .build();

        ProjectDocument saved = projectRepository.save(project);
        auditService.record(
                AuditEventType.PROJECT_CREATED,
                managerId,
                "PROJECT",
                saved.getId()
        );
        return ProjectResponse.from(saved);
    }

    public List<ProjectResponse> findAll(ProjectStatus status) {
        List<ProjectDocument> projects = status == null
                ? projectRepository.findAllByDeletedAtIsNullOrderByUpdatedAtDesc()
                : projectRepository.findAllByStatusAndDeletedAtIsNull(status);
        return projects.stream().map(ProjectResponse::from).toList();
    }

    public ProjectResponse findById(String id) {
        return ProjectResponse.from(findExisting(id));
    }

    public ProjectResponse update(
            String id,
            UpdateProjectRequest request,
            String actorId
    ) {
        ProjectDocument project = findExisting(id);
        if (!Objects.equals(
                project.getStrategicGuidelineId(),
                request.strategicGuidelineId()
        )) {
            guidelineService.requireActiveAndEffective(
                    request.strategicGuidelineId()
            );
        }
        validateDates(request.startDate(), request.expectedEndDate());
        ProjectStatus previousStatus = project.getStatus();

        project.setStrategicGuidelineId(request.strategicGuidelineId());
        project.setName(request.name().strip());
        project.setDescription(request.description().strip());
        project.setStatus(request.status());
        project.setStage(request.stage().strip());
        project.setStartDate(request.startDate());
        project.setExpectedEndDate(request.expectedEndDate());
        project.setCompletedAt(completedAt(request.status(), project.getCompletedAt()));
        project.setInvestment(request.investment());
        project.setFinancialReturn(request.financialReturn());
        project.setCostReduction(request.costReduction());
        project.setProductivityGain(request.productivityGain());

        ProjectDocument saved = projectRepository.save(project);
        auditService.record(
                previousStatus != request.status()
                        ? AuditEventType.PROJECT_STATUS_CHANGED
                        : AuditEventType.PROJECT_UPDATED,
                actorId,
                "PROJECT",
                saved.getId()
        );
        return ProjectResponse.from(saved);
    }

    public void delete(String id, String actorId) {
        ProjectDocument project = findExisting(id);
        project.setDeletedAt(Instant.now());
        project.setDeletedBy(actorId);
        projectRepository.save(project);

        auditService.record(
                AuditEventType.PROJECT_DELETED,
                actorId,
                "PROJECT",
                project.getId()
        );
    }

    public ProjectCreationResult createFromApprovedIdea(
            IdeaDocument idea,
            String managerId
    ) {
        return projectRepository.findBySourceIdeaId(idea.getId())
                .map(project -> new ProjectCreationResult(project, false))
                .orElseGet(() -> persistProjectFromIdea(idea, managerId));
    }

    private ProjectCreationResult persistProjectFromIdea(
            IdeaDocument idea,
            String managerId
    ) {
        var project = ProjectDocument.builder()
                .sourceIdeaId(idea.getId())
                .strategicGuidelineId(idea.getStrategicGuidelineId())
                .name(idea.getTitle())
                .description(idea.getDescription())
                .managerId(managerId)
                .status(ProjectStatus.PLANNED)
                .stage("PLANEJAMENTO")
                .investment(BigDecimal.ZERO)
                .financialReturn(BigDecimal.ZERO)
                .costReduction(BigDecimal.ZERO)
                .productivityGain(BigDecimal.ZERO)
                .build();

        try {
            ProjectDocument saved = projectRepository.save(project);
            auditService.record(
                    AuditEventType.PROJECT_CREATED,
                    managerId,
                    "PROJECT",
                    saved.getId()
            );
            return new ProjectCreationResult(saved, true);
        } catch (DuplicateKeyException exception) {
            ProjectDocument existing = projectRepository
                    .findBySourceIdeaId(idea.getId())
                    .orElseThrow(() -> exception);
            return new ProjectCreationResult(existing, false);
        }
    }

    private ProjectDocument findExisting(String id) {
        MongoIdValidator.requireValid(id);
        return projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "PROJECT_NOT_FOUND",
                        "Project was not found"
                ));
    }

    private void validateDates(LocalDate startDate, LocalDate expectedEndDate) {
        if (expectedEndDate.isBefore(startDate)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_PROJECT_PERIOD",
                    "expectedEndDate must be equal to or later than startDate"
            );
        }
    }

    private Instant completedAt(ProjectStatus status, Instant currentValue) {
        if (status == ProjectStatus.COMPLETED) {
            return currentValue == null ? Instant.now() : currentValue;
        }
        return null;
    }

    public record ProjectCreationResult(ProjectDocument project, boolean created) {
    }
}
