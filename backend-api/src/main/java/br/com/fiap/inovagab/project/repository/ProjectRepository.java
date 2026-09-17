package br.com.fiap.inovagab.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.fiap.inovagab.project.document.ProjectDocument;
import br.com.fiap.inovagab.project.model.ProjectStatus;

public interface ProjectRepository extends MongoRepository<ProjectDocument, String> {

    Optional<ProjectDocument> findBySourceIdeaId(String sourceIdeaId);

    Optional<ProjectDocument> findByIdAndDeletedAtIsNull(String id);

    List<ProjectDocument> findAllByStatusAndDeletedAtIsNull(ProjectStatus status);

    List<ProjectDocument> findAllByStrategicGuidelineIdAndDeletedAtIsNull(
            String strategicGuidelineId
    );

    List<ProjectDocument> findAllByDeletedAtIsNullOrderByUpdatedAtDesc();
}
