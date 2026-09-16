package br.com.fiap.inovagab.guideline.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import br.com.fiap.inovagab.guideline.document.StrategicGuidelineDocument;

public interface StrategicGuidelineRepository
        extends MongoRepository<StrategicGuidelineDocument, String> {

    @Query("""
            {
              'status': 'ACTIVE',
              'deletedAt': null,
              '$and': [
                { '$or': [ { 'validFrom': null }, { 'validFrom': { '$lte': ?0 } } ] },
                { '$or': [ { 'validUntil': null }, { 'validUntil': { '$gte': ?0 } } ] }
              ]
            }
            """)
    List<StrategicGuidelineDocument> findActiveAndEffectiveAt(Instant instant);

    List<StrategicGuidelineDocument> findAllByDeletedAtIsNullOrderByUpdatedAtDesc();
}
