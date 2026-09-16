package br.com.fiap.inovagab.idea.controller;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.inovagab.idea.dto.CreateIdeaRequest;
import br.com.fiap.inovagab.idea.dto.IdeaApprovalResponse;
import br.com.fiap.inovagab.idea.dto.IdeaDecisionRequest;
import br.com.fiap.inovagab.idea.dto.IdeaResponse;
import br.com.fiap.inovagab.idea.dto.PrioritizeIdeaRequest;
import br.com.fiap.inovagab.idea.dto.UpdateIdeaRequest;
import br.com.fiap.inovagab.idea.model.IdeaStatus;
import br.com.fiap.inovagab.idea.service.IdeaService;

@RestController
@RequestMapping("/v1/ideas")
@RequiredArgsConstructor
public class IdeaController {

    private final IdeaService ideaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('OPERADOR')")
    public IdeaResponse create(
            @Valid @RequestBody CreateIdeaRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ideaService.create(request, jwt.getSubject());
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('OPERADOR')")
    public List<IdeaResponse> findMine(@AuthenticationPrincipal Jwt jwt) {
        return ideaService.findMine(jwt.getSubject());
    }

    @GetMapping
    @PreAuthorize("hasRole('GESTOR')")
    public List<IdeaResponse> findAll(
            @RequestParam(required = false) IdeaStatus status
    ) {
        return ideaService.findAll(status);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'GESTOR')")
    public IdeaResponse findById(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ideaService.findById(
                id,
                jwt.getSubject(),
                hasRole(jwt, "GESTOR")
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public IdeaResponse update(
            @PathVariable String id,
            @Valid @RequestBody UpdateIdeaRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ideaService.update(id, request, jwt.getSubject());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('OPERADOR')")
    public void delete(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ideaService.delete(id, jwt.getSubject());
    }

    @PatchMapping("/{id}/priority")
    @PreAuthorize("hasRole('GESTOR')")
    public IdeaResponse prioritize(
            @PathVariable String id,
            @Valid @RequestBody PrioritizeIdeaRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ideaService.prioritize(id, request, jwt.getSubject());
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('GESTOR')")
    public IdeaApprovalResponse approve(
            @PathVariable String id,
            @Valid @RequestBody IdeaDecisionRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ideaService.approve(id, request, jwt.getSubject());
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('GESTOR')")
    public IdeaResponse reject(
            @PathVariable String id,
            @Valid @RequestBody IdeaDecisionRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ideaService.reject(id, request, jwt.getSubject());
    }

    private boolean hasRole(Jwt jwt, String role) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles != null && roles.contains(role);
    }
}
