// GeocodingService.java
package com.cartaGo.cartaGo_backend.service;

import com.cartaGo.cartaGo_backend.dto.GeocodingDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class GeocodingService {

    // Requisito de Nominatim: identifica tu app y un email de contacto
    private static final String USER_AGENT = "carta-go/1.0 (albargrrss@gmail.com)";
    private static final String BASE = "https://nominatim.openstreetmap.org";
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /** Dirección → Coordenadas (y dirección formateada) */
    public GeocodingDTO buscarPorDireccion(String direccion) {
        if (direccion == null || direccion.isBlank()) {
            throw new IllegalArgumentException("La dirección no puede estar vacía");
        }

        String url = BASE + "/search?format=jsonv2&limit=1&q=" + urlEncode(direccion);

        JsonNode first = getJsonArrayFirst(url);
        if (first == null) {
            throw notFound("No se encontró la dirección");
        }

        double lat = parseDouble(first.get("lat"));
        double lon = parseDouble(first.get("lon"));
        String display = text(first.get("display_name"));

        return GeocodingDTO.builder()
                .lat(lat)
                .lon(lon)
                .direccion(display)
                .build();
    }

    /** Coordenadas → Dirección (y te devolvemos las mismas coords) */
    public GeocodingDTO buscarPorCoordenadas(double lat, double lon) {
        String url = BASE + "/reverse?format=jsonv2&lat=" + lat + "&lon=" + lon;

        JsonNode root = getJson(url);
        if (root == null || root.get("display_name") == null) {
            throw notFound("No se encontró dirección para esas coordenadas");
        }

        String display = text(root.get("display_name"));

        return GeocodingDTO.builder()
                .lat(lat)
                .lon(lon)
                .direccion(display)
                .build();
    }

    // ==================== helpers simples ====================

    private JsonNode getJson(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() >= 200 && res.statusCode() < 300) {
                return mapper.readTree(res.body());
            }
            throw new RuntimeException("Error HTTP " + res.statusCode() + " al llamar a Nominatim");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Fallo llamando a Nominatim: " + e.getMessage(), e);
        }
    }

    private JsonNode getJsonArrayFirst(String url) {
        JsonNode arr = getJson(url);
        if (arr != null && arr.isArray() && arr.size() > 0) {
            return arr.get(0);
        }
        return null;
    }

    private static String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static double parseDouble(JsonNode n) {
        if (n == null || n.isNull()) return 0.0;
        // Nominatim devuelve lat/lon como string. Lo convertimos a double.
        return Double.parseDouble(n.asText());
    }

    private static String text(JsonNode n) {
        return (n == null || n.isNull()) ? "" : n.asText();
    }

    private static RuntimeException notFound(String msg) {
        // Lanza la excepción que prefieras; aquí una Runtime simple
        return new RuntimeException(msg);
    }
}
