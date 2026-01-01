package com.griddb.volcanic.controller;

import com.griddb.volcanic.service.GridDBService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final GridDBService gridDBService;

    public DashboardController(GridDBService gridDBService) {
        this.gridDBService = gridDBService;
    }

    @GetMapping("/")
    public String getDashboard(Model model) {
        model.addAttribute("volcanoData", gridDBService.getVolcanoData());
        model.addAttribute("seismicData", gridDBService.getSeismicData());
        return "dashboard";
    }
}
