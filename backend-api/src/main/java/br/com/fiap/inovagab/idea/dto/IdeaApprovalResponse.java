package br.com.fiap.inovagab.idea.dto;

import br.com.fiap.inovagab.project.dto.ProjectResponse;

public record IdeaApprovalResponse(
        IdeaResponse idea,
        ProjectResponse project,
        boolean projectCreated
) {
}
