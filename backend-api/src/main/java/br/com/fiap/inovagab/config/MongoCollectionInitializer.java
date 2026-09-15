package br.com.fiap.inovagab.config;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import br.com.fiap.inovagab.audit.document.AuditEventDocument;
import br.com.fiap.inovagab.guideline.document.StrategicGuidelineDocument;
import br.com.fiap.inovagab.idea.document.IdeaDocument;
import br.com.fiap.inovagab.project.document.ProjectDocument;
import br.com.fiap.inovagab.user.document.UserDocument;

@Slf4j
@Component
@RequiredArgsConstructor
public class MongoCollectionInitializer {

    private static final List<Class<?>> DOCUMENT_TYPES = List.of(
            UserDocument.class,
            StrategicGuidelineDocument.class,
            IdeaDocument.class,
            ProjectDocument.class,
            AuditEventDocument.class
    );

    private final MongoTemplate mongoTemplate;

    @EventListener(ContextRefreshedEvent.class)
    public void initializeCollectionsAndIndexes() {
        var indexResolver = new MongoPersistentEntityIndexResolver(
                mongoTemplate.getConverter().getMappingContext()
        );

        DOCUMENT_TYPES.forEach(documentType -> initialize(documentType, indexResolver));
    }

    private void initialize(
            Class<?> documentType,
            MongoPersistentEntityIndexResolver indexResolver
    ) {
        String collectionName = mongoTemplate.getCollectionName(documentType);

        if (!mongoTemplate.collectionExists(documentType)) {
            mongoTemplate.createCollection(documentType);
            log.info("MongoDB collection created: {}", collectionName);
        }

        var indexOperations = mongoTemplate.indexOps(documentType);
        indexResolver.resolveIndexFor(documentType)
                .forEach(indexOperations::ensureIndex);

        log.info("MongoDB indexes verified: {}", collectionName);
    }
}
