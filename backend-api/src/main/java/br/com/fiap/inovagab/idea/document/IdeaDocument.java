package br.com.fiap.inovagab.idea.document;

import java.math.BigDecimal;
import java.time.Instant;

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

import br.com.fiap.inovagab.idea.model.IdeaPriority;
import br.com.fiap.inovagab.idea.model.IdeaStatus;
import br.com.fiap.inovagab.shared.document.AuditableDocument;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ideas")
@CompoundIndexes({
        @CompoundIndex(name = "ix_ideas_author_status", def = "{'authorId': 1, 'status': 1}")
})
public class IdeaDocument extends AuditableDocument {

    @MongoId(FieldType.OBJECT_ID)
    private String id;

    @Field("title")
    private String title;

    @Field("description")
    private String description;

    @Field("category")
    private String category;

    @Indexed(name = "ix_ideas_author")
    @Field("authorId")
    private String authorId;

    @Indexed(name = "ix_ideas_guideline")
    @Field("strategicGuidelineId")
    private String strategicGuidelineId;

    @Builder.Default
    @Indexed(name = "ix_ideas_status")
    @Field("status")
    private IdeaStatus status = IdeaStatus.SUBMITTED;

    @Field("priority")
    private IdeaPriority priority;

    @Field(name = "managerScore", targetType = FieldType.DECIMAL128)
    private BigDecimal managerScore;

    @Field(name = "aiScore", targetType = FieldType.DECIMAL128)
    private BigDecimal aiScore;

    @Field("aiJustification")
    private String aiJustification;

    @Field("aiProvider")
    private String aiProvider;

    @Field("aiModel")
    private String aiModel;

    @Field("aiPromptVersion")
    private String aiPromptVersion;

    @Field("aiAnalyzedAt")
    private Instant aiAnalyzedAt;

    @Field("managerComment")
    private String managerComment;

    @Field("evaluatedBy")
    private String evaluatedBy;

    @Field("evaluatedAt")
    private Instant evaluatedAt;
}

