package br.com.fiap.inovagab.idea.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.fiap.inovagab.idea.document.IdeaDocument;
import br.com.fiap.inovagab.idea.model.IdeaStatus;

public interface IdeaRepository extends MongoRepository<IdeaDocument, String> {

    List<IdeaDocument> findAllByAuthorIdOrderByCreatedAtDesc(String authorId);

    List<IdeaDocument> findAllByStatusOrderByCreatedAtAsc(IdeaStatus status);

    List<IdeaDocument> findAllByStrategicGuidelineId(String strategicGuidelineId);

    List<IdeaDocument> findAllByOrderByCreatedAtDesc();
}
