package com.cartaGo.cartaGo_backend.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class GeocodingService {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;

    public GeocodingService(
            RestTemplate restTemplate,
            @Value("${geocode.mapsco.base-url}") String baseUrl,
            @Value("${geocode.mapsco.api-key}") String apiKey
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        // LOG TEMPORAL: no imprimas la clave completa
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("[Geocode] API key VACÍA o NULA");
        } else {
            System.out.println("[Geocode] API key OK (len=" + apiKey.length() + ", empieza por "
                    + apiKey.substring(0, Math.min(4, apiKey.length())) + "…)");
        }
    }

    // ===== DTOs mínimos =====
    public record ReverseGeocodeResult(
            @JsonProperty("display_name") String displayName,
            String lat,
            String lon,
            Map<String, Object> address
    ) {}

    public record ForwardGeocodeResult(
            @JsonProperty("display_name") String displayName,
            String lat,
            String lon,
            Map<String, Object> address
    ) {}

    // ===== lat/lon -> dirección =====
    public ReverseGeocodeResult reverse(double lat, double lon) {
        String url = String.format(
                "%s/reverse?lat=%s&lon=%s&format=jsonv2&api_key=%s",
                baseUrl, lat, lon, apiKey
        );
        return restTemplate.getForObject(url, ReverseGeocodeResult.class);
    }

    // ===== dirección -> lista de candidatos (lat/lon, display_name, etc.) =====
    public List<ForwardGeocodeResult> forward(String q) {
        String url = String.format(
                "%s/search?q=%s&format=jsonv2&api_key=%s",
                baseUrl, URLEncoder.encode(q, StandardCharsets.UTF_8), apiKey
        );

        var response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ForwardGeocodeResult>>() {}
        );
        return response.getBody();
    }


}
