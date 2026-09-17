package br.com.fiap.inovagab.idea.service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import br.com.fiap.inovagab.guideline.document.StrategicGuidelineDocument;
import br.com.fiap.inovagab.guideline.repository.StrategicGuidelineRepository;
import br.com.fiap.inovagab.idea.dto.AiEvaluationResponse;
import br.com.fiap.inovagab.idea.model.IdeaPriority;
import br.com.fiap.inovagab.idea.model.IdeaStatus;
import br.com.fiap.inovagab.shared.exception.ApiException;

@Service
@RequiredArgsConstructor
public class AiEvaluationService {

    private final IdeaService ideaService;
    private final StrategicGuidelineRepository guidelineRepository;
    private final GeminiClient geminiClient;
    private final ObjectMapper mapper;

    public AiEvaluationResponse evaluate(String ideaId) {
        var idea = ideaService.requireExisting(ideaId);
        if (idea.getStatus() != IdeaStatus.SUBMITTED && idea.getStatus() != IdeaStatus.UNDER_REVIEW) {
            throw new ApiException(HttpStatus.CONFLICT, "INVALID_IDEA_STATUS",
                    "The idea is no longer open for evaluation");
        }
        List<StrategicGuidelineDocument> guidelines =
                guidelineRepository.findActiveAndEffectiveAt(Instant.now());
        if (guidelines.isEmpty()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "NO_ACTIVE_GUIDELINES",
                    "No active strategic guidelines are available");
        }
        String guidelineContext = guidelines.stream()
                .map(item -> "ID: %s; título: %s; descrição: %s; categoria: %s; campanha: %s"
                        .formatted(item.getId(), item.getTitle(), item.getDescription(),
                                item.getCategory(), item.getCampaign()))
                .collect(Collectors.joining("\n"));
        String context = """
                Avalie o alinhamento estratégico da ideia abaixo. Responda somente em JSON com
                score inteiro de 0 a 100, priority LOW/MEDIUM/HIGH/CRITICAL, strategyIds
                selecionados apenas da lista de diretrizes e reason em português. A resposta
                é uma sugestão; a decisão final é do Gestor.
                Ideia: %s
                Descrição: %s
                Categoria: %s
                Diretriz vinculada: %s
                Diretrizes vigentes:
                %s
                """.formatted(idea.getTitle(), idea.getDescription(), idea.getCategory(),
                idea.getStrategicGuidelineId(), guidelineContext);
        return validate(geminiClient.evaluate(context),
                guidelines.stream().map(StrategicGuidelineDocument::getId).collect(Collectors.toSet()));
    }

    AiEvaluationResponse validate(String raw, Set<String> validIds) {
        try {
            JsonNode root = mapper.readTree(raw);
            if (root == null || !root.isObject() || !root.path("score").isIntegralNumber()) {
                throw invalidResponse();
            }
            int score = root.path("score").intValue();
            if (score < 0 || score > 100) throw invalidResponse();
            IdeaPriority priority = IdeaPriority.valueOf(root.path("priority").asText());
            JsonNode idsNode = root.path("strategyIds");
            if (!idsNode.isArray() || idsNode.isEmpty()) throw invalidResponse();
            List<String> ids = new java.util.ArrayList<>();
            for (JsonNode idNode : idsNode) {
                if (!idNode.isTextual() || !validIds.contains(idNode.asText())) throw invalidResponse();
                if (!ids.contains(idNode.asText())) ids.add(idNode.asText());
            }
            String reason = root.path("reason").asText().strip();
            if (reason.isBlank()) throw invalidResponse();
            return new AiEvaluationResponse(score, priority, List.copyOf(ids), reason);
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw invalidResponse();
        }
    }

    private ApiException invalidResponse() {
        return new ApiException(HttpStatus.BAD_GATEWAY, "AI_INVALID_RESPONSE",
                "Gemini returned an invalid evaluation");
    }
}
