package br.com.fiap.inovagab.guideline.document;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import br.com.fiap.inovagab.guideline.model.GuidelineStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuidelineVersionSnapshot {

    @Field("version")
    private Integer version;

    @Field("title")
    private String title;

    @Field("description")
    private String description;

    @Field("category")
    private String category;

    @Field("campaign")
    private String campaign;

    @Field("status")
    private GuidelineStatus status;

    @Field("validFrom")
    private Instant validFrom;

    @Field("validUntil")
    private Instant validUntil;

    @Field("changedBy")
    private String changedBy;

    @Field("changedAt")
    private Instant changedAt;
}

