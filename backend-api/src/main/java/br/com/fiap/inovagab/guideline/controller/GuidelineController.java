package br.com.fiap.inovagab.guideline.controller;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.inovagab.guideline.dto.CreateGuidelineRequest;
import br.com.fiap.inovagab.guideline.dto.GuidelineResponse;
import br.com.fiap.inovagab.guideline.dto.UpdateGuidelineRequest;
import br.com.fiap.inovagab.guideline.service.GuidelineService;

@RestController
@RequestMapping("/v1/guidelines")
@RequiredArgsConstructor
public class GuidelineController {

    private final GuidelineService guidelineService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<GuidelineResponse> findAll(@AuthenticationPrincipal Jwt jwt) {
        return guidelineService.findAll(hasRole(jwt, "LIDER"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public GuidelineResponse findById(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return guidelineService.findById(id, hasRole(jwt, "LIDER"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('LIDER')")
    public GuidelineResponse create(
            @Valid @RequestBody CreateGuidelineRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return guidelineService.create(request, jwt.getSubject());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIDER')")
    public GuidelineResponse update(
            @PathVariable String id,
            @Valid @RequestBody UpdateGuidelineRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return guidelineService.update(id, request, jwt.getSubject());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('LIDER')")
    public void delete(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        guidelineService.delete(id, jwt.getSubject());
    }

    private boolean hasRole(Jwt jwt, String role) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles != null && roles.contains(role);
    }
}
