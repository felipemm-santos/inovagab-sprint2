package br.com.fiap.inovagab.audit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import br.com.fiap.inovagab.audit.document.AuditEventDocument;
import br.com.fiap.inovagab.audit.model.AuditEventType;
import br.com.fiap.inovagab.audit.repository.AuditEventRepository;
import br.com.fiap.inovagab.shared.web.RequestIdFilter;
import br.com.fiap.inovagab.user.model.UserRole;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessAuditService {

    private final AuditEventRepository auditEventRepository;

    public void record(
            AuditEventType eventType,
            String actorId,
            String entityType,
            String entityId
    ) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserRole actorRole = authentication == null
                ? null
                : authentication.getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .filter(authority -> authority.startsWith("ROLE_"))
                        .map(authority -> authority.substring("ROLE_".length()))
                        .map(this::toRole)
                        .filter(java.util.Objects::nonNull)
                        .findFirst()
                        .orElse(null);

        persist(AuditEventDocument.builder()
                .eventType(eventType)
                .actorId(actorId)
                .actorRole(actorRole)
                .entityType(entityType)
                .entityId(entityId)
                .requestId(currentRequestId())
                .build());
    }

    private UserRole toRole(String value) {
        try {
            return UserRole.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String currentRequestId() {
        if (RequestContextHolder.getRequestAttributes()
                instanceof ServletRequestAttributes attributes) {
            Object requestId = attributes.getRequest().getAttribute(
                    RequestIdFilter.REQUEST_ID_ATTRIBUTE
            );
            return requestId == null ? null : requestId.toString();
        }
        return null;
    }

    private void persist(AuditEventDocument event) {
        try {
            auditEventRepository.save(event);
        } catch (RuntimeException exception) {
            log.warn(
                    "Business audit event could not be persisted: {}",
                    exception.getClass().getSimpleName()
            );
        }
    }
}
