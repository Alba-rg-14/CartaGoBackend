package com.cartaGo.cartaGo_backend.service;

import com.cartaGo.cartaGo_backend.dto.AddressDTO;
import com.cartaGo.cartaGo_backend.dto.GeocodingDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;

import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static org.springframework.http.HttpStatus.NOT_FOUND;


@Service
public class GeocodingService {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;
    public AddressDTO adress;

    public GeocodingService(RestTemplate restTemplate,
                            @Value("${geocode.mapsco.base-url}") String baseUrl,
                            @Value("${geocode.mapsco.api-key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }


    // ===== modelos internos solo para parseo =====
    private record ReverseRs(
            @JsonProperty("display_name") String displayName,
            String lat,
            String lon,
            Map<String, Object> address
    ) {}
    private record ForwardRs(
            @JsonProperty("display_name") String displayName,
            String lat,
            String lon,
            Map<String, Object> address
    ) {}

    // ========== REVERSE: lat/lon -> dirección estructurada ==========
    public GeocodingDTO reverse(double lat, double lon) {
        String url = String.format(
                "%s/reverse?lat=%s&lon=%s&format=jsonv2&addressdetails=1&accept-language=es&limit=1&api_key=%s",
                baseUrl, lat, lon, apiKey
        );
        ReverseRs res = restTemplate.getForObject(url, ReverseRs.class);
        if (res == null) throw new ResponseStatusException(NOT_FOUND, "No hay resultado para esas coordenadas");

        AddressDTO address = mapAddress(res.address());
        return new GeocodingDTO(res.displayName(), toDouble(res.lat()), toDouble(res.lon()), address);
    }

    private GeocodingDTO tryForwardStructured(AddressDTO a) {
        if (a == null) return null;

        // street = "road houseNumber" (si hay número)
        String street = isNotBlank(a.houseNumber())
                ? (safe(a.road()) + " " + a.houseNumber()).trim()
                : safe(a.road());

        // city: city/town/village ya lo resolvemos al mapear; aquí usamos el que venga
        String city = safe(a.city());
        String county = safe(a.province()); // a veces la “province” del usuario coincide con county
        String state = safe(a.state());
        String postal = safe(a.postcode());
        String country = safe(a.country());
        String cc = safe(a.countryCode()).toLowerCase(); // ej: "es"

        // Si no hay nada útil, no hacemos estructurada
        boolean hasAny = isNotBlank(street) || isNotBlank(city) || isNotBlank(postal) || isNotBlank(country);
        if (!hasAny) return null;

        String url = String.format(
                "%s/search?format=jsonv2&addressdetails=1&accept-language=es&limit=1%s%s%s%s%s%s%s%s",
                baseUrl,
                isNotBlank(street) ? "&street=" + enc(street) : "",
                isNotBlank(city)   ? "&city=" + enc(city) : "",
                isNotBlank(county) ? "&county=" + enc(county) : "",
                isNotBlank(state)  ? "&state=" + enc(state) : "",
                isNotBlank(postal) ? "&postalcode=" + enc(postal) : "",
                isNotBlank(country)? "&country=" + enc(country) : "",
                isNotBlank(cc)     ? "&countrycodes=" + enc(cc) : "",
                isNotBlank(apiKey) ? "&api_key=" + enc(apiKey) : ""
        );

        // LOG para depurar
        System.out.println("[geocode] structured URL: " + url);

        var response = restTemplate.exchange(url, HttpMethod.GET, null,
                new org.springframework.core.ParameterizedTypeReference<java.util.List<ForwardRs>>() {});
        var body = response.getBody();
        if (body == null || body.isEmpty()) return null;

        var first = body.get(0);
        AddressDTO addr = mapAddress(first.address());
        return new GeocodingDTO(first.displayName(), toDouble(first.lat()), toDouble(first.lon()), addr);
    }


    // ========== FORWARD: AddressDTO -> un candidato ==========
    public GeocodingDTO forwardOne(AddressDTO addr) {
        // PRIMERO: estructurada
        GeocodingDTO hit = tryForwardStructured(addr);
        if (hit != null) return hit;

        // DESPUÉS: variantes de texto libre
        String q1 = toSingleLine(addr);          // tal cual
        String q2 = normalizeAscii(q1);          // sin tildes
        String q3 = removeHouseNumber(addr);     // sin número + sin tildes
        String q4 = minimalLine(addr);           // básico normalizado

        for (String q : java.util.List.of(q1, q2, q3, q4)) {
            if (q == null || q.isBlank()) continue;
            System.out.println("[geocode] free-text query: " + q); // LOG
            GeocodingDTO res = tryForward(q);
            if (res != null) return res;
        }
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND,
                "No se encontró la dirección con las variantes probadas"
        );
    }

    private static String enc(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }

    // ===== helper forward (limit=1, addressdetails=1) =====
    private GeocodingDTO tryForward(String q) {
        String url = String.format(
                "%s/search?q=%s&format=jsonv2&addressdetails=1&accept-language=es&limit=1&api_key=%s",
                baseUrl, URLEncoder.encode(q, StandardCharsets.UTF_8), apiKey
        );
        var response = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<ForwardRs>>() {});
        List<ForwardRs> body = response.getBody();
        if (body == null || body.isEmpty()) return null;

        var first = body.get(0);
        AddressDTO addr = mapAddress(first.address());
        return new GeocodingDTO(first.displayName(), toDouble(first.lat()), toDouble(first.lon()), addr);
    }

    // ===== tu "single line" AHORA en el service (recibe AddressDTO) =====
    public String toSingleLine(AddressDTO a) {
        if (a == null) return null;
        StringBuilder sb = new StringBuilder();
        append(sb, a.road());
        if (isNotBlank(a.houseNumber())) sb.append(" ").append(a.houseNumber());
        appendComma(sb, a.city());
        appendComma(sb, a.province());
        appendComma(sb, a.state());
        appendComma(sb, a.postcode());
        appendComma(sb, a.country());
        return sb.toString();
    }

    // ===== helpers de variantes =====
    private String removeHouseNumber(AddressDTO a) {
        AddressDTO noNum = new AddressDTO(
                a.road(), null, a.neighbourhood(), a.borough(),
                a.city(), a.province(), a.state(), a.postcode(), a.country(), a.countryCode()
        );
        return normalizeAscii(toSingleLine(noNum));
    }
    private String minimalLine(AddressDTO a) {
        String s = String.join(", ",
                notBlank(a.road()),
                notBlank(a.city()),
                notBlank(a.postcode()),
                notBlank(a.country())
        ).replaceAll("^,\\s*|,\\s*,", "");
        return normalizeAscii(s);
    }

    // ===== mapeo de address flexible =====
    private AddressDTO mapAddress(Map<String, Object> a) {
        if (a == null) return new AddressDTO(null,null,null,null,null,null,null,null,null,null);
        String road   = s(a.get("road"));
        String house  = s(a.get("house_number"));
        String neigh  = s(a.get("neighbourhood"));
        String bor    = s(a.get("borough"));
        String city   = firstNonBlank(s(a.get("city")), s(a.get("town")), s(a.get("village")));
        String county = s(a.get("county"));
        String prov   = firstNonBlank(s(a.get("province")), county);
        String state  = s(a.get("state"));
        String cp     = s(a.get("postcode"));
        String country= s(a.get("country"));
        String cc     = s(a.get("country_code"));
        return new AddressDTO(road, house, neigh, bor, city, prov, state, cp, country, cc);
    }

    // ===== utils =====
    private static String s(Object o) { return o == null ? null : String.valueOf(o); }
    private static Double toDouble(String s) {
        try { return s == null ? null : Double.valueOf(s); } catch (Exception e) { return null; }
    }
    private static String firstNonBlank(String... ss) {
        for (String x : ss) if (x != null && !x.isBlank()) return x;
        return null;
    }
    private static String notBlank(String s) { return (s == null || s.isBlank()) ? "" : s; }
    private static void append(StringBuilder sb, String s) {
        if (s != null && !s.isBlank()) sb.append(s);
    }
    private static void appendComma(StringBuilder sb, String s) {
        if (s != null && !s.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(s);
        }
    }
    private static String normalizeAscii(String s) {
        if (s == null) return null;
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return n.replaceAll("\\s+", " ").trim();
    }


    private static String safe(String s) {
        return (s == null) ? "" : s;
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }



}
