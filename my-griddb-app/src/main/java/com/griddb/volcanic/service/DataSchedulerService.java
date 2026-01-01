package com.griddb.volcanic.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DataSchedulerService {

    private final VolcanicDataService volcanicDataService;
    private final GridDBService gridDBService;

    public DataSchedulerService(VolcanicDataService volcanicDataService, GridDBService gridDBService) {
        this.volcanicDataService = volcanicDataService;
        this.gridDBService = gridDBService;
    }

    @Scheduled(fixedRate = 300000) // Fetch data every 5 minutes
    public void fetchDataAndStore() throws IOException {
        gridDBService.putVolcanoData(volcanicDataService.fetchVolcanoData());
        gridDBService.putSeismicData(volcanicDataService.fetchSeismicData());
    }
}
