package com.cartaGo.cartaGo_backend.controller;

import com.cartaGo.cartaGo_backend.dto.AddressDTO;
import com.cartaGo.cartaGo_backend.dto.GeocodingDTO;
import com.cartaGo.cartaGo_backend.service.GeocodingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/geocode")
public class GeocodingController {

    private final GeocodingService geocodingService;

    public GeocodingController(GeocodingService service) {
        this.geocodingService = service;
    }


    // GET /geocode/reverse?lat=36.721&lon=-4.421
    @GetMapping("/reverse")
    public GeocodingDTO reverse(@RequestParam double lat, @RequestParam double lon) {
        return geocodingService.reverse(lat, lon);
    }


    // POST /geocode/forward   (body: AddressDTO)
    // Ej. {
    //   "road": "Paseo del Parque",
    //   "houseNumber": "3",
    //   "city": "Málaga",
    //   "postcode": "29015",
    //   "country": "España"
    // }
    @PostMapping("/forward")
    public GeocodingDTO forward(@RequestBody AddressDTO address) {
        return geocodingService.forwardOne(address);
    }
}
