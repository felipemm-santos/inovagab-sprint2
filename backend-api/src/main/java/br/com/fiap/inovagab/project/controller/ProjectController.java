package br.com.fiap.inovagab.project.controller;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.inovagab.project.dto.CreateProjectRequest;
import br.com.fiap.inovagab.project.dto.ProjectResponse;
import br.com.fiap.inovagab.project.dto.UpdateProjectRequest;
import br.com.fiap.inovagab.project.model.ProjectStatus;
import br.com.fiap.inovagab.project.service.ProjectService;

@RestController
@RequestMapping("/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR', 'LIDER')")
    public List<ProjectResponse> findAll(
            @RequestParam(required = false) ProjectStatus status
    ) {
        return projectService.findAll(status);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR', 'LIDER')")
    public ProjectResponse findById(@PathVariable String id) {
        return projectService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('GESTOR')")
    public ProjectResponse create(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return projectService.create(request, jwt.getSubject());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ProjectResponse update(
            @PathVariable String id,
            @Valid @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return projectService.update(id, request, jwt.getSubject());
    }
}
