package br.com.fiap.inovagab.guideline.document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import br.com.fiap.inovagab.guideline.model.GuidelineStatus;
import br.com.fiap.inovagab.shared.document.AuditableDocument;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "strategic_guidelines")
@CompoundIndexes({
        @CompoundIndex(
                name = "ix_guidelines_validity",
                def = "{'validFrom': 1, 'validUntil': 1}"
        )
})
public class StrategicGuidelineDocument extends AuditableDocument {

    @MongoId(FieldType.OBJECT_ID)
    private String id;

    @Field("title")
    private String title;

    @Field("description")
    private String description;

    @Indexed(name = "ix_guidelines_category")
    @Field("category")
    private String category;

    @Field("campaign")
    private String campaign;

    @Builder.Default
    @Indexed(name = "ix_guidelines_status")
    @Field("status")
    private GuidelineStatus status = GuidelineStatus.DRAFT;

    @Field("validFrom")
    private Instant validFrom;

    @Field("validUntil")
    private Instant validUntil;

    @Builder.Default
    @Field("version")
    private Integer version = 1;

    @CreatedBy
    @Field("createdBy")
    private String createdBy;

    @LastModifiedBy
    @Field("updatedBy")
    private String updatedBy;

    @Builder.Default
    @Field("history")
    private List<GuidelineVersionSnapshot> history = new ArrayList<>();

    @Field("deletedAt")
    private Instant deletedAt;

    @Field("deletedBy")
    private String deletedBy;
}

