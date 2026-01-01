package com.griddb.volcanic.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.griddb.volcanic.model.SeismicData;
import com.griddb.volcanic.model.VolcanoData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class VolcanicDataService {

    private static final Logger logger = LoggerFactory.getLogger(VolcanicDataService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public VolcanicDataService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<VolcanoData> fetchVolcanoData() throws IOException {
        String url = "https://volcanoes.usgs.gov/hans-public/api/volcano/getUSVolcanoes";
        String response = restTemplate.getForObject(url, String.class);
        JsonNode root = objectMapper.readTree(response);
        List<VolcanoData> volcanoDataList = new ArrayList<>();
        for (JsonNode node : root) {
            VolcanoData volcanoData = new VolcanoData();
            volcanoData.timestamp = Instant.now();
            volcanoData.name = node.get("volcano_name").asText();
            String threatLevel = node.get("nvews_threat").asText();
            if (threatLevel.isEmpty()) {
                volcanoData.alertLevel = "UNASSIGNED";
                volcanoData.colorCode = "GREY";
            } else {
                volcanoData.alertLevel = threatLevel;
                switch (threatLevel) {
                    case "Very High Threat":
                        volcanoData.colorCode = "RED";
                        break;
                    case "High Threat":
                        volcanoData.colorCode = "ORANGE";
                        break;
                    case "Moderate Threat":
                        volcanoData.colorCode = "YELLOW";
                        break;
                    case "Low Threat":
                    case "Very Low Threat":
                        volcanoData.colorCode = "GREEN";
                        break;
                    default:
                        volcanoData.colorCode = "GREY";
                        break;
                }
            }
            volcanoData.latitude = node.get("latitude").asDouble();
            volcanoData.longitude = node.get("longitude").asDouble();
            volcanoDataList.add(volcanoData);
        }
        return volcanoDataList;
    }

    public List<SeismicData> fetchSeismicData() throws IOException {
        String url = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.geojson";
        String response = restTemplate.getForObject(url, String.class);
        JsonNode root = objectMapper.readTree(response);
        JsonNode features = root.get("features");
        List<SeismicData> seismicDataList = new ArrayList<>();
        for (JsonNode feature : features) {
            try {
                JsonNode properties = feature.get("properties");
                JsonNode geometry = feature.get("geometry");
                
                long time = properties.get("time").asLong();
                double latitude = geometry.get("coordinates").get(1).asDouble();
                double longitude = geometry.get("coordinates").get(0).asDouble();
                double depth = geometry.get("coordinates").get(2).asDouble();
                double mag = properties.get("mag").asDouble();
                String place = properties.get("place").asText();

                logger.debug("Seismic record: time={}, lat={}, lon={}, depth={}, mag={}, place={}", 
                    time, latitude, longitude, depth, mag, place);

                if (depth > 1000) {
                    logger.warn("Skipping seismic record due to unreasonable depth: {}", depth);
                    continue; // Skip records with unreasonable depth
                }

                SeismicData seismicData = new SeismicData();
                seismicData.time = Instant.ofEpochMilli(time);
                seismicData.latitude = latitude;
                seismicData.longitude = longitude;
                seismicData.depth = depth;
                seismicData.mag = mag;
                seismicData.place = place;
                seismicDataList.add(seismicData);
            } catch (Exception e) {
                logger.error("Failed to parse seismic record: {}", feature, e);
            }
        }
        return seismicDataList;
    }
}
