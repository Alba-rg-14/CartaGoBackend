package com.cartaGo.cartaGo_backend.controller;

import com.cartaGo.cartaGo_backend.service.GeocodingService;
import com.cartaGo.cartaGo_backend.service.GeocodingService.ForwardGeocodeResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/geocode")
public class GeocodingController {

    private final GeocodingService service;

    public GeocodingController(GeocodingService service) {
        this.service = service;
    }

    // GET /api/geocode/reverse?lat=36.7213&lon=-4.4214
    @GetMapping("/reverse")
    public Map<String, Object> reverse(@RequestParam double lat, @RequestParam double lon) {
        var r = service.reverse(lat, lon);
        return Map.of(
                "displayName", r.displayName(),
                "lat", r.lat(),
                "lon", r.lon(),
                "address", r.address()
        );
    }

    // GET /api/geocode/forward?q=Av.%20de%20Andaluc%C3%ADa%2C%20M%C3%A1laga
    @GetMapping("/forward")
    public List<ForwardGeocodeResult> forward(@RequestParam String q) {
        return service.forward(q);
    }
}
