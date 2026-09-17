package br.com.fiap.inovagab.idea.service;

import java.util.Set;
import java.util.List;
import java.time.Instant;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import br.com.fiap.inovagab.idea.model.IdeaPriority;
import br.com.fiap.inovagab.idea.model.IdeaStatus;
import br.com.fiap.inovagab.idea.document.IdeaDocument;
import br.com.fiap.inovagab.guideline.document.StrategicGuidelineDocument;
import br.com.fiap.inovagab.guideline.repository.StrategicGuidelineRepository;
import br.com.fiap.inovagab.shared.exception.ApiException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class AiEvaluationServiceTest {

    private final AiEvaluationService service =
            new AiEvaluationService(null, null, null, new ObjectMapper());

    @Test
    void acceptsValidSuggestionWithoutPersistingManagerDecision() {
        var result = service.validate("""
                {"score":85,"priority":"HIGH","strategyIds":["g1"],"reason":"Alinhamento forte"}
                """, Set.of("g1"));
        assertThat(result.score()).isEqualTo(85);
        assertThat(result.priority()).isEqualTo(IdeaPriority.HIGH);
        assertThat(result.strategyIds()).containsExactly("g1");
    }

    @Test
    void rejectsMalformedOrUntrustedSuggestion() {
        for (String raw : new String[] {
                "", "not json",
                "{\"score\":101,\"priority\":\"HIGH\",\"strategyIds\":[\"g1\"],\"reason\":\"ok\"}",
                "{\"score\":85,\"priority\":\"UNKNOWN\",\"strategyIds\":[\"g1\"],\"reason\":\"ok\"}",
                "{\"score\":85,\"priority\":\"HIGH\",\"strategyIds\":[\"missing\"],\"reason\":\"ok\"}"
        }) {
            assertThatThrownBy(() -> service.validate(raw, Set.of("g1")))
                    .isInstanceOfSatisfying(ApiException.class, exception -> {
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
                        assertThat(exception.getCode()).isEqualTo("AI_INVALID_RESPONSE");
                    });
        }
    }

    @Test
    void evaluatesUsingActiveGuidelinesAndLeavesManualDecisionUntouched() {
        IdeaService ideas = mock(IdeaService.class);
        StrategicGuidelineRepository guidelines = mock(StrategicGuidelineRepository.class);
        GeminiClient client = mock(GeminiClient.class);
        IdeaDocument idea = IdeaDocument.builder().id("idea1").title("Otimizar embarque")
                .description("Reduzir filas").category("Operações")
                .strategicGuidelineId("g1").status(IdeaStatus.SUBMITTED).build();
        StrategicGuidelineDocument guideline = StrategicGuidelineDocument.builder()
                .id("g1").title("Eficiência").description("Melhorar processos")
                .category("Operações").campaign("Ano atual").build();
        when(ideas.requireExisting("idea1")).thenReturn(idea);
        when(guidelines.findActiveAndEffectiveAt(org.mockito.ArgumentMatchers.any(Instant.class)))
                .thenReturn(List.of(guideline));
        when(client.evaluate(org.mockito.ArgumentMatchers.contains("Eficiência")))
                .thenReturn("{\"score\":85,\"priority\":\"HIGH\",\"strategyIds\":[\"g1\"],\"reason\":\"Alinhamento\"}");

        var result = new AiEvaluationService(ideas, guidelines, client, new ObjectMapper())
                .evaluate("idea1");

        assertThat(result.score()).isEqualTo(85);
        assertThat(idea.getManagerScore()).isNull();
        assertThat(idea.getPriority()).isNull();
        verify(ideas).requireExisting("idea1");
    }
}
