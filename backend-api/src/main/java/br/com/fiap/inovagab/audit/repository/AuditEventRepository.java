package br.com.fiap.inovagab.audit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.fiap.inovagab.audit.document.AuditEventDocument;

public interface AuditEventRepository extends MongoRepository<AuditEventDocument, String> {

    Page<AuditEventDocument> findAllByActorIdOrderByTimestampDesc(
            String actorId,
            Pageable pageable
    );

    Page<AuditEventDocument> findAllByEntityTypeAndEntityIdOrderByTimestampDesc(
            String entityType,
            String entityId,
            Pageable pageable
    );
}

