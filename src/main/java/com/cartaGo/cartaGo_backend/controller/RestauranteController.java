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
}
