package com.cartaGo.cartaGo_backend.controller;

import com.cartaGo.cartaGo_backend.dto.GeocodingDTO;
import com.cartaGo.cartaGo_backend.service.GeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/geocoding")
@RequiredArgsConstructor
public class GeocodingController {

    private final GeocodingService geocodingService;

    /** Dirección -> Coordenadas (+ dirección formateada) */
    @GetMapping("/forward")
    public GeocodingDTO forward(@RequestParam String direccion) {
        try {
            return geocodingService.buscarPorDireccion(direccion);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    /** Coordenadas -> Dirección (+ te devolvemos las mismas coords) */
    @GetMapping("/reverse")
    public GeocodingDTO reverse(@RequestParam double lat, @RequestParam double lon) {
        try {
            return geocodingService.buscarPorCoordenadas(lat, lon);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }
}