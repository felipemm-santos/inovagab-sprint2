package br.com.fiap.inovagab.guideline.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import br.com.fiap.inovagab.audit.model.AuditEventType;
import br.com.fiap.inovagab.audit.service.BusinessAuditService;
import br.com.fiap.inovagab.guideline.document.GuidelineVersionSnapshot;
import br.com.fiap.inovagab.guideline.document.StrategicGuidelineDocument;
import br.com.fiap.inovagab.guideline.dto.CreateGuidelineRequest;
import br.com.fiap.inovagab.guideline.dto.GuidelineResponse;
import br.com.fiap.inovagab.guideline.dto.UpdateGuidelineRequest;
import br.com.fiap.inovagab.guideline.model.GuidelineStatus;
import br.com.fiap.inovagab.guideline.repository.StrategicGuidelineRepository;
import br.com.fiap.inovagab.shared.exception.ApiException;
import br.com.fiap.inovagab.shared.validation.MongoIdValidator;

@Service
@RequiredArgsConstructor
public class GuidelineService {

    private final StrategicGuidelineRepository guidelineRepository;
    private final BusinessAuditService auditService;

    public GuidelineResponse create(CreateGuidelineRequest request, String actorId) {
        validateValidity(request.validFrom(), request.validUntil());

        var guideline = StrategicGuidelineDocument.builder()
                .title(request.title().strip())
                .description(request.description().strip())
                .category(request.category().strip())
                .campaign(request.campaign().strip())
                .status(request.status())
                .validFrom(request.validFrom())
                .validUntil(request.validUntil())
                .version(1)
                .createdBy(actorId)
                .updatedBy(actorId)
                .build();

        StrategicGuidelineDocument saved = guidelineRepository.save(guideline);
        auditService.record(
                AuditEventType.GUIDELINE_CREATED,
                actorId,
                "GUIDELINE",
                saved.getId()
        );
        return GuidelineResponse.from(saved);
    }

    public List<GuidelineResponse> findAll(boolean leader) {
        List<StrategicGuidelineDocument> guidelines = leader
                ? guidelineRepository.findAllByDeletedAtIsNullOrderByUpdatedAtDesc()
                : guidelineRepository.findActiveAndEffectiveAt(Instant.now());

        return guidelines.stream()
                .map(GuidelineResponse::from)
                .toList();
    }

    public GuidelineResponse findById(String id, boolean leader) {
        StrategicGuidelineDocument guideline = findExisting(id);
        if (!leader && !isActiveAndEffective(guideline, Instant.now())) {
            throw notFound();
        }
        return GuidelineResponse.from(guideline);
    }

    public StrategicGuidelineDocument requireActiveAndEffective(String id) {
        StrategicGuidelineDocument guideline = findExisting(id);
        if (!isActiveAndEffective(guideline, Instant.now())) {
            throw new ApiException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "GUIDELINE_NOT_ACTIVE",
                    "The selected strategic guideline is not active and effective"
            );
        }
        return guideline;
    }

    public StrategicGuidelineDocument requireExisting(String id) {
        return findExisting(id);
    }

    public GuidelineResponse update(
            String id,
            UpdateGuidelineRequest request,
            String actorId
    ) {
        validateValidity(request.validFrom(), request.validUntil());
        StrategicGuidelineDocument guideline = findExisting(id);
        Instant now = Instant.now();

        List<GuidelineVersionSnapshot> history = guideline.getHistory();
        if (history == null) {
            history = new ArrayList<>();
            guideline.setHistory(history);
        }
        history.add(snapshot(guideline, actorId, now));

        guideline.setTitle(request.title().strip());
        guideline.setDescription(request.description().strip());
        guideline.setCategory(request.category().strip());
        guideline.setCampaign(request.campaign().strip());
        guideline.setStatus(request.status());
        guideline.setValidFrom(request.validFrom());
        guideline.setValidUntil(request.validUntil());
        guideline.setVersion(currentVersion(guideline) + 1);
        guideline.setUpdatedBy(actorId);

        StrategicGuidelineDocument saved = guidelineRepository.save(guideline);
        auditService.record(
                request.status() == GuidelineStatus.ARCHIVED
                        ? AuditEventType.GUIDELINE_ARCHIVED
                        : AuditEventType.GUIDELINE_UPDATED,
                actorId,
                "GUIDELINE",
                saved.getId()
        );
        return GuidelineResponse.from(saved);
    }

    public void delete(String id, String actorId) {
        StrategicGuidelineDocument guideline = findExisting(id);
        Instant now = Instant.now();

        List<GuidelineVersionSnapshot> history = guideline.getHistory();
        if (history == null) {
            history = new ArrayList<>();
            guideline.setHistory(history);
        }
        history.add(snapshot(guideline, actorId, now));

        guideline.setStatus(GuidelineStatus.ARCHIVED);
        guideline.setVersion(currentVersion(guideline) + 1);
        guideline.setDeletedAt(now);
        guideline.setDeletedBy(actorId);
        guideline.setUpdatedBy(actorId);
        guidelineRepository.save(guideline);

        auditService.record(
                AuditEventType.GUIDELINE_DELETED,
                actorId,
                "GUIDELINE",
                guideline.getId()
        );
    }

    private StrategicGuidelineDocument findExisting(String id) {
        MongoIdValidator.requireValid(id);
        return guidelineRepository.findById(id)
                .filter(guideline -> guideline.getDeletedAt() == null)
                .orElseThrow(this::notFound);
    }

    private boolean isActiveAndEffective(
            StrategicGuidelineDocument guideline,
            Instant instant
    ) {
        boolean started = guideline.getValidFrom() == null
                || !instant.isBefore(guideline.getValidFrom());
        boolean notExpired = guideline.getValidUntil() == null
                || !instant.isAfter(guideline.getValidUntil());
        return guideline.getStatus() == GuidelineStatus.ACTIVE
                && guideline.getDeletedAt() == null
                && started
                && notExpired;
    }

    private void validateValidity(Instant validFrom, Instant validUntil) {
        if (validFrom != null
                && validUntil != null
                && validUntil.isBefore(validFrom)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_VALIDITY_PERIOD",
                    "validUntil must be equal to or later than validFrom"
            );
        }
    }

    private GuidelineVersionSnapshot snapshot(
            StrategicGuidelineDocument guideline,
            String actorId,
            Instant changedAt
    ) {
        return GuidelineVersionSnapshot.builder()
                .version(currentVersion(guideline))
                .title(guideline.getTitle())
                .description(guideline.getDescription())
                .category(guideline.getCategory())
                .campaign(guideline.getCampaign())
                .status(guideline.getStatus())
                .validFrom(guideline.getValidFrom())
                .validUntil(guideline.getValidUntil())
                .changedBy(actorId)
                .changedAt(changedAt)
                .build();
    }

    private int currentVersion(StrategicGuidelineDocument guideline) {
        return guideline.getVersion() == null ? 1 : guideline.getVersion();
    }

    private ApiException notFound() {
        return new ApiException(
                HttpStatus.NOT_FOUND,
                "GUIDELINE_NOT_FOUND",
                "Strategic guideline was not found"
        );
    }
}
