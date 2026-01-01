package com.griddb.volcanic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.griddb.volcanic.model.GridDbResponse;
import com.griddb.volcanic.model.SeismicData;
import com.griddb.volcanic.model.VolcanoData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GridDBService {

    private static final Logger logger = LoggerFactory.getLogger(GridDBService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${griddb.rest.url}")
    private String griddbRestUrl;

    @Value("${griddb.api.key}")
    private String griddbApiKey;

    // Define a formatter that always includes milliseconds, even if zero
    private static final DateTimeFormatter ISO_INSTANT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                                                    .withZone(ZoneOffset.UTC);

    public GridDBService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        // Ensure Instant is serialized as ISO-8601 string, not a timestamp
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + griddbApiKey);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    public void putVolcanoData(List<VolcanoData> volcanoData) {
        List<List<Object>> payload = new ArrayList<>();
        for (VolcanoData data : volcanoData) {
            List<Object> row = new ArrayList<>();
            row.add(ISO_INSTANT_FORMATTER.format(data.timestamp));
            row.add(data.name);
            row.add(data.alertLevel);
            row.add(data.colorCode);
            row.add(data.latitude);
            row.add(data.longitude);
            payload.add(row);
        }
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            logger.debug("Sending payload to GridDB for volcanoes: {}", payloadJson);
            HttpEntity<String> request = new HttpEntity<>(payloadJson, createHeaders());
            restTemplate.exchange(griddbRestUrl + "/containers/volcanoes/rows", HttpMethod.PUT, request, Void.class);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing volcano payload to JSON", e);
        }
    }

    public void putSeismicData(List<SeismicData> seismicData) {
        List<List<Object>> payload = new ArrayList<>();
        for (SeismicData data : seismicData) {
            List<Object> row = new ArrayList<>();
            row.add(ISO_INSTANT_FORMATTER.format(data.time));
            row.add(data.latitude);
            row.add(data.longitude);
            row.add(data.depth);
            row.add(data.mag);
            row.add(data.place);
            payload.add(row);
        }
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            logger.debug("Sending payload to GridDB for seismic data: {}", payloadJson);
            HttpEntity<String> request = new HttpEntity<>(payloadJson, createHeaders());
            restTemplate.exchange(griddbRestUrl + "/containers/seismic/rows", HttpMethod.PUT, request, Void.class);
        } catch (Exception e) {
            logger.error("Failed to send seismic record: {}", payload, e);
        }
    }
    
    public List<VolcanoData> getVolcanoData() {
        String requestBody = "{\"offset\": 0, \"limit\": 55555}";
        HttpEntity<String> request = new HttpEntity<>(requestBody, createHeaders());
        try {
            ResponseEntity<GridDbResponse> response = restTemplate.exchange(
                    griddbRestUrl + "/containers/volcanoes/rows",
                    HttpMethod.POST,
                    request,
                    GridDbResponse.class
            );
            logger.info("Response from GridDB for volcano data: {}", response.getBody());
            GridDbResponse responseBody = response.getBody();
            if (responseBody == null || responseBody.rows == null) {
                return Collections.emptyList();
            }

            List<VolcanoData> result = new ArrayList<>();
            for (List<Object> row : responseBody.rows) {
                VolcanoData data = new VolcanoData();
                data.timestamp = Instant.parse((String) row.get(0));
                data.name = (String) row.get(1);
                data.alertLevel = (String) row.get(2);
                data.colorCode = (String) row.get(3);
                data.latitude = ((Number) row.get(4)).doubleValue();
                data.longitude = ((Number) row.get(5)).doubleValue();
                result.add(data);
            }
            return result;
        } catch (Exception e) {
            logger.error("Error retrieving volcano data from GridDB", e);
            return Collections.emptyList();
        }
    }

    public List<SeismicData> getSeismicData() {
        String requestBody = "{\"offset\": 0, \"limit\": 55555}";
        HttpEntity<String> request = new HttpEntity<>(requestBody, createHeaders());
        try {
            ResponseEntity<GridDbResponse> response = restTemplate.exchange(
                    griddbRestUrl + "/containers/seismic/rows",
                    HttpMethod.POST,
                    request,
                    GridDbResponse.class
            );
            logger.info("Response from GridDB for seismic data: {}", response.getBody());
            GridDbResponse responseBody = response.getBody();
            if (responseBody == null || responseBody.rows == null) {
                return Collections.emptyList();
            }

            List<SeismicData> result = new ArrayList<>();
            for (List<Object> row : responseBody.rows) {
                SeismicData data = new SeismicData();
                // Assuming the timestamp retrieved from GridDB will always include milliseconds
                data.time = Instant.parse((String) row.get(0));
                data.latitude = ((Number) row.get(1)).doubleValue();
                data.longitude = ((Number) row.get(2)).doubleValue();
                data.depth = ((Number) row.get(3)).doubleValue();
                data.mag = ((Number) row.get(4)).doubleValue();
                data.place = (String) row.get(5);
                result.add(data);
            }
            return result;
        } catch (Exception e) {
            logger.error("Error retrieving seismic data from GridDB", e);
            return Collections.emptyList();
        }
    }
}