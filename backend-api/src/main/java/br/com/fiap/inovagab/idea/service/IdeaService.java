package br.com.fiap.inovagab.idea.service;

import java.time.Instant;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import br.com.fiap.inovagab.audit.model.AuditEventType;
import br.com.fiap.inovagab.audit.service.BusinessAuditService;
import br.com.fiap.inovagab.guideline.service.GuidelineService;
import br.com.fiap.inovagab.idea.document.IdeaDocument;
import br.com.fiap.inovagab.idea.dto.CreateIdeaRequest;
import br.com.fiap.inovagab.idea.dto.IdeaApprovalResponse;
import br.com.fiap.inovagab.idea.dto.IdeaDecisionRequest;
import br.com.fiap.inovagab.idea.dto.IdeaResponse;
import br.com.fiap.inovagab.idea.dto.PrioritizeIdeaRequest;
import br.com.fiap.inovagab.idea.dto.UpdateIdeaRequest;
import br.com.fiap.inovagab.idea.model.IdeaStatus;
import br.com.fiap.inovagab.idea.repository.IdeaRepository;
import br.com.fiap.inovagab.project.dto.ProjectResponse;
import br.com.fiap.inovagab.project.service.ProjectService;
import br.com.fiap.inovagab.shared.exception.ApiException;
import br.com.fiap.inovagab.shared.validation.MongoIdValidator;

@Service
@RequiredArgsConstructor
public class IdeaService {

    private final IdeaRepository ideaRepository;
    private final GuidelineService guidelineService;
    private final ProjectService projectService;
    private final BusinessAuditService auditService;

    public IdeaResponse create(CreateIdeaRequest request, String authorId) {
        guidelineService.requireActiveAndEffective(request.strategicGuidelineId());

        var idea = IdeaDocument.builder()
                .title(request.title().strip())
                .description(request.description().strip())
                .category(request.category().strip())
                .authorId(authorId)
                .strategicGuidelineId(request.strategicGuidelineId())
                .status(IdeaStatus.SUBMITTED)
                .build();

        IdeaDocument saved = ideaRepository.save(idea);
        auditService.record(
                AuditEventType.IDEA_CREATED,
                authorId,
                "IDEA",
                saved.getId()
        );
        return IdeaResponse.from(saved);
    }

    public List<IdeaResponse> findMine(String authorId) {
        return ideaRepository.findAllByAuthorIdOrderByCreatedAtDesc(authorId)
                .stream()
                .map(IdeaResponse::from)
                .toList();
    }

    public List<IdeaResponse> findAll(IdeaStatus status) {
        List<IdeaDocument> ideas = status == null
                ? ideaRepository.findAllByOrderByCreatedAtDesc()
                : ideaRepository.findAllByStatusOrderByCreatedAtAsc(status);
        return ideas.stream().map(IdeaResponse::from).toList();
    }

    public IdeaResponse findById(
            String id,
            String authenticatedUserId,
            boolean manager
    ) {
        IdeaDocument idea = findExisting(id);
        if (!manager) {
            requireOwnership(idea, authenticatedUserId);
        }
        return IdeaResponse.from(idea);
    }

    public IdeaResponse update(
            String id,
            UpdateIdeaRequest request,
            String authorId
    ) {
        IdeaDocument idea = findExisting(id);
        requireOwnership(idea, authorId);
        requireSubmitted(idea);
        guidelineService.requireActiveAndEffective(request.strategicGuidelineId());

        idea.setTitle(request.title().strip());
        idea.setDescription(request.description().strip());
        idea.setCategory(request.category().strip());
        idea.setStrategicGuidelineId(request.strategicGuidelineId());
        IdeaDocument saved = ideaRepository.save(idea);

        auditService.record(
                AuditEventType.IDEA_UPDATED,
                authorId,
                "IDEA",
                saved.getId()
        );
        return IdeaResponse.from(saved);
    }

    public void delete(String id, String authorId) {
        IdeaDocument idea = findExisting(id);
        requireOwnership(idea, authorId);
        requireSubmitted(idea);
        ideaRepository.delete(idea);

        auditService.record(
                AuditEventType.IDEA_DELETED,
                authorId,
                "IDEA",
                idea.getId()
        );
    }

    public IdeaResponse prioritize(
            String id,
            PrioritizeIdeaRequest request,
            String managerId
    ) {
        IdeaDocument idea = findExisting(id);
        requireOpenForEvaluation(idea);

        idea.setPriority(request.priority());
        idea.setManagerScore(request.managerScore());
        idea.setStatus(IdeaStatus.UNDER_REVIEW);
        idea.setEvaluatedBy(managerId);
        IdeaDocument saved = ideaRepository.save(idea);

        auditService.record(
                AuditEventType.IDEA_PRIORITIZED,
                managerId,
                "IDEA",
                saved.getId()
        );
        return IdeaResponse.from(saved);
    }

    public IdeaApprovalResponse approve(
            String id,
            IdeaDecisionRequest request,
            String managerId
    ) {
        IdeaDocument idea = findExisting(id);
        if (idea.getStatus() == IdeaStatus.REJECTED) {
            throw invalidStatus("A rejected idea cannot be approved");
        }

        if (idea.getStatus() != IdeaStatus.APPROVED) {
            idea.setStatus(IdeaStatus.APPROVED);
            idea.setManagerComment(request.comment().strip());
            idea.setEvaluatedBy(managerId);
            idea.setEvaluatedAt(Instant.now());
            idea = ideaRepository.save(idea);

            auditService.record(
                    AuditEventType.IDEA_APPROVED,
                    managerId,
                    "IDEA",
                    idea.getId()
            );
        }

        ProjectService.ProjectCreationResult projectResult =
                projectService.createFromApprovedIdea(idea, managerId);
        return new IdeaApprovalResponse(
                IdeaResponse.from(idea),
                ProjectResponse.from(projectResult.project()),
                projectResult.created()
        );
    }

    public IdeaResponse reject(
            String id,
            IdeaDecisionRequest request,
            String managerId
    ) {
        IdeaDocument idea = findExisting(id);
        requireOpenForEvaluation(idea);

        idea.setStatus(IdeaStatus.REJECTED);
        idea.setManagerComment(request.comment().strip());
        idea.setEvaluatedBy(managerId);
        idea.setEvaluatedAt(Instant.now());
        IdeaDocument saved = ideaRepository.save(idea);

        auditService.record(
                AuditEventType.IDEA_REJECTED,
                managerId,
                "IDEA",
                saved.getId()
        );
        return IdeaResponse.from(saved);
    }

    private IdeaDocument findExisting(String id) {
        MongoIdValidator.requireValid(id);
        return ideaRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "IDEA_NOT_FOUND",
                        "Idea was not found"
                ));
    }

    public IdeaDocument requireExisting(String id) {
        return findExisting(id);
    }

    private void requireOwnership(IdeaDocument idea, String userId) {
        if (!idea.getAuthorId().equals(userId)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "IDEA_ACCESS_DENIED",
                    "Only the author can access or change this idea"
            );
        }
    }

    private void requireSubmitted(IdeaDocument idea) {
        if (idea.getStatus() != IdeaStatus.SUBMITTED) {
            throw invalidStatus("The idea can only be changed while it is submitted");
        }
    }

    private void requireOpenForEvaluation(IdeaDocument idea) {
        if (idea.getStatus() != IdeaStatus.SUBMITTED
                && idea.getStatus() != IdeaStatus.UNDER_REVIEW) {
            throw invalidStatus("The idea is no longer open for evaluation");
        }
    }

    private ApiException invalidStatus(String message) {
        return new ApiException(
                HttpStatus.CONFLICT,
                "INVALID_IDEA_STATUS",
                message
        );
    }
}
