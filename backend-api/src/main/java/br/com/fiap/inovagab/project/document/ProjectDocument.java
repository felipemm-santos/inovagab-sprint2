package br.com.fiap.inovagab.project.document;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import br.com.fiap.inovagab.project.model.ProjectStatus;
import br.com.fiap.inovagab.shared.document.AuditableDocument;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "projects")
@CompoundIndexes({
        @CompoundIndex(name = "ix_projects_guideline_status", def = "{'strategicGuidelineId': 1, 'status': 1}")
})
public class ProjectDocument extends AuditableDocument {

    @MongoId(FieldType.OBJECT_ID)
    private String id;

    @Indexed(name = "ux_projects_source_idea", unique = true, sparse = true)
    @Field("sourceIdeaId")
    private String sourceIdeaId;

    @Indexed(name = "ix_projects_guideline")
    @Field("strategicGuidelineId")
    private String strategicGuidelineId;

    @Field("name")
    private String name;

    @Field("description")
    private String description;

    @Field("managerId")
    private String managerId;

    @Builder.Default
    @Indexed(name = "ix_projects_status")
    @Field("status")
    private ProjectStatus status = ProjectStatus.PLANNED;

    @Field("stage")
    private String stage;

    @Field("startDate")
    private LocalDate startDate;

    @Field("expectedEndDate")
    private LocalDate expectedEndDate;

    @Field("completedAt")
    private Instant completedAt;

    @Indexed(name = "ix_projects_deleted_at")
    @Field("deletedAt")
    private Instant deletedAt;

    @Field("deletedBy")
    private String deletedBy;

    @Builder.Default
    @Field(name = "investment", targetType = FieldType.DECIMAL128)
    private BigDecimal investment = BigDecimal.ZERO;

    @Builder.Default
    @Field(name = "financialReturn", targetType = FieldType.DECIMAL128)
    private BigDecimal financialReturn = BigDecimal.ZERO;

    @Builder.Default
    @Field(name = "costReduction", targetType = FieldType.DECIMAL128)
    private BigDecimal costReduction = BigDecimal.ZERO;

    @Builder.Default
    @Field(name = "productivityGain", targetType = FieldType.DECIMAL128)
    private BigDecimal productivityGain = BigDecimal.ZERO;
}
