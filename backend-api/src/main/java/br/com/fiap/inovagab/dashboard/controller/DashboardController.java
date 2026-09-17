package br.com.fiap.inovagab.dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.inovagab.dashboard.dto.DashboardSummaryResponse;
import br.com.fiap.inovagab.dashboard.dto.ProjectDashboardResponse;
import br.com.fiap.inovagab.dashboard.dto.StrategyDashboardResponse;
import br.com.fiap.inovagab.dashboard.service.DashboardService;

@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LIDER')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/projects/{projectId}")
    public ProjectDashboardResponse getProjectReport(
            @PathVariable String projectId
    ) {
        return dashboardService.getProjectReport(projectId);
    }

    @GetMapping("/strategies/{guidelineId}")
    public StrategyDashboardResponse getStrategyReport(
            @PathVariable String guidelineId
    ) {
        return dashboardService.getStrategyReport(guidelineId);
    }
}
