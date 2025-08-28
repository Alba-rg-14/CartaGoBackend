package com.cartaGo.cartaGo_backend.controller;

import com.cartaGo.cartaGo_backend.dto.RestauranteDTO;
import com.cartaGo.cartaGo_backend.dto.RestaurantePreviewDTO;
import com.cartaGo.cartaGo_backend.service.RestauranteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/restaurante")
public class RestauranteController {

    private final RestauranteService restauranteService;

    @GetMapping
    public List<RestauranteDTO> getAll(){
        return restauranteService.getAllRestaurantes();
    }

    @GetMapping("/{id}")
    public RestaurantePreviewDTO getById(@PathVariable Integer id){
        return restauranteService.getById(id);
    }

    @GetMapping("preview")
    public List<RestaurantePreviewDTO> getAllPreview(){
        return restauranteService.getAllRestaurantesPreviewDTO();
    }

    @GetMapping("preview/{nombre}")
    public RestauranteDTO getByNombre(@PathVariable String nombre){
       return restauranteService.getRestaurantesPreviewDTOByNombre(nombre);
    }

    @GetMapping("preview/abiertos")
    public List<RestaurantePreviewDTO> getAbiertos(){
        return restauranteService.getRestaurantesPreviewDTOAbiertos();
    }

    @PutMapping("/estado/{id}")
    public void cambiarEstado(@PathVariable Integer id){
        restauranteService.cambiarEstado(id);
    }

    // /restaurante/cerca?lat=36.721&lon=-4.421&radioKm=1
    @GetMapping("/cerca")
    public List<RestauranteDTO> getCercanos(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(required = false, defaultValue = "1.0") double radioKm
    ) {
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180 || radioKm <= 0) {
            throw new IllegalArgumentException("Parámetros inválidos: lat/lon/radioKm");
        }
        return restauranteService.findCercanos(lat, lon, radioKm);
    }
}
