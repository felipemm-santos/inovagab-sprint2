package br.com.fiap.inovagab.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;

import br.com.fiap.inovagab.MongoIntegrationTestSupport;
import br.com.fiap.inovagab.audit.document.AuditEventDocument;
import br.com.fiap.inovagab.guideline.document.StrategicGuidelineDocument;
import br.com.fiap.inovagab.idea.document.IdeaDocument;
import br.com.fiap.inovagab.project.document.ProjectDocument;
import br.com.fiap.inovagab.user.document.UserDocument;

@SpringBootTest
class MongoCollectionInitializerTest
        extends MongoIntegrationTestSupport {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void shouldCreateAllCollections() {
        assertThat(mongoTemplate.getCollectionNames())
                .contains(
                        "users",
                        "strategic_guidelines",
                        "ideas",
                        "projects",
                        "audit_events"
                );
    }

    @Test
    void shouldCreateUserIndexes() {
        assertThat(indexNames(UserDocument.class))
                .contains(
                        "_id_",
                        "ux_users_email"
                );
    }

    @Test
    void shouldCreateGuidelineIndexes() {
        assertThat(indexNames(StrategicGuidelineDocument.class))
                .contains(
                        "_id_",
                        "ix_guidelines_category",
                        "ix_guidelines_status",
                        "ix_guidelines_validity"
                );
    }

    @Test
    void shouldCreateIdeaIndexes() {
        assertThat(indexNames(IdeaDocument.class))
                .contains(
                        "_id_",
                        "ix_ideas_author",
                        "ix_ideas_status",
                        "ix_ideas_guideline",
                        "ix_ideas_author_status"
                );
    }

    @Test
    void shouldCreateProjectIndexes() {
        assertThat(indexNames(ProjectDocument.class))
                .contains(
                        "_id_",
                        "ux_projects_source_idea",
                        "ix_projects_status",
                        "ix_projects_guideline",
                        "ix_projects_deleted_at",
                        "ix_projects_guideline_status"
                );
    }

    @Test
    void shouldCreateAuditIndexes() {
        assertThat(indexNames(AuditEventDocument.class))
                .contains(
                        "_id_",
                        "ix_audit_timestamp",
                        "ix_audit_actor",
                        "ix_audit_entity"
                );
    }

    @Test
    void sourceIdeaIndexShouldBeUniqueAndSparse() {
        IndexInfo index = findIndex(
                ProjectDocument.class,
                "ux_projects_source_idea"
        );

        assertThat(index.isUnique()).isTrue();
        assertThat(index.isSparse()).isTrue();
    }

    @Test
    void userEmailIndexShouldBeUnique() {
        IndexInfo index = findIndex(
                UserDocument.class,
                "ux_users_email"
        );

        assertThat(index.isUnique()).isTrue();
    }

    private Set<String> indexNames(Class<?> documentType) {
        return mongoTemplate
                .indexOps(documentType)
                .getIndexInfo()
                .stream()
                .map(IndexInfo::getName)
                .collect(Collectors.toSet());
    }

    private IndexInfo findIndex(
            Class<?> documentType,
            String indexName
    ) {
        return mongoTemplate
                .indexOps(documentType)
                .getIndexInfo()
                .stream()
                .filter(index -> indexName.equals(index.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Índice não encontrado: " + indexName
                ));
    }
}
