package br.com.fiap.inovagab.audit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import br.com.fiap.inovagab.audit.document.AuditEventDocument;
import br.com.fiap.inovagab.audit.model.AuditEventType;
import br.com.fiap.inovagab.audit.repository.AuditEventRepository;
import br.com.fiap.inovagab.user.document.UserDocument;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationAuditService {

    private final AuditEventRepository auditEventRepository;

    public void recordSuccess(UserDocument user) {
        persist(AuditEventDocument.builder()
                .eventType(AuditEventType.USER_LOGIN_SUCCESS)
                .actorId(user.getId())
                .actorRole(user.getRole())
                .entityType("USER")
                .entityId(user.getId())
                .build());
    }

    public void recordFailure() {
        persist(AuditEventDocument.builder()
                .eventType(AuditEventType.USER_LOGIN_FAILURE)
                .entityType("USER")
                .build());
    }

    private void persist(AuditEventDocument event) {
        try {
            auditEventRepository.save(event);
        } catch (RuntimeException exception) {
            log.warn(
                    "Authentication audit event could not be persisted: {}",
                    exception.getClass().getSimpleName()
            );
        }
    }
}
