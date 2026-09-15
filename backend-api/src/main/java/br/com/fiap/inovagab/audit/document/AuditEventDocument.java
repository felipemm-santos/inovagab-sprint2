package br.com.fiap.inovagab.audit.document;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import br.com.fiap.inovagab.audit.model.AuditEventType;
import br.com.fiap.inovagab.user.model.UserRole;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_events")
@CompoundIndexes({
        @CompoundIndex(name = "ix_audit_entity", def = "{'entityType': 1, 'entityId': 1}")
})
public class AuditEventDocument {

    @MongoId(FieldType.OBJECT_ID)
    private String id;

    @Field("eventType")
    private AuditEventType eventType;

    @Indexed(name = "ix_audit_actor")
    @Field("actorId")
    private String actorId;

    @Field("actorRole")
    private UserRole actorRole;

    @Field("entityType")
    private String entityType;

    @Field("entityId")
    private String entityId;

    @Builder.Default
    @Indexed(name = "ix_audit_timestamp", direction = IndexDirection.DESCENDING)
    @Field("timestamp")
    private Instant timestamp = Instant.now();

    @Field("requestId")
    private String requestId;

    @Builder.Default
    @Field("metadata")
    private Map<String, Object> metadata = new LinkedHashMap<>();
}

