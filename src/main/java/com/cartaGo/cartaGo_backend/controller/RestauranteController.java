package com.cartaGo.cartaGo_backend.controller;

import com.cartaGo.cartaGo_backend.dto.CartaDTO;
import com.cartaGo.cartaGo_backend.dto.RestauranteDTO;
import com.cartaGo.cartaGo_backend.dto.RestaurantePreviewDTO;
import com.cartaGo.cartaGo_backend.dto.RestauranteUpdateDTO;
import com.cartaGo.cartaGo_backend.entity.Carta;
import com.cartaGo.cartaGo_backend.service.RestauranteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @PutMapping("/{id}")
    public RestauranteDTO actualizarRestaurante(@PathVariable Integer id,
                                                @RequestBody RestauranteUpdateDTO req) {
        return restauranteService.actualizarRestaurante(id, req);
    }

    /** Actualizar ubicación a partir de dirección */
    @PutMapping("/{id}/ubicacion")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setUbicacion(@PathVariable Integer id, @RequestParam String direccion) throws Exception {
        restauranteService.setRestauranteUbicacion(id, direccion);
    }

    /** Actualizar ubicación a partir de coordenadas */
    @PutMapping("/{id}/coordenadas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setCoordenadas(@PathVariable Integer id,
                               @RequestParam Double lat,
                               @RequestParam Double lon) {
        restauranteService.setRestauranteCoor(id, lat, lon);
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

    @PutMapping(path = "/{id}/imagen", consumes = "text/plain", produces = "text/plain")
    public String setImagen(@PathVariable Integer id, @RequestBody String url) {
        return restauranteService.setImagen(id, url); // devolvemos la misma URL guardada
    }

    @GetMapping(path = "/{id}/imagen", produces = "text/plain")
    public String getImagen(@PathVariable Integer id) {
        return restauranteService.getImagen(id);
    }

    // DELETE imagen
    @DeleteMapping("/{id}/imagen")
    public void deleteImagen(@PathVariable Integer id) {
        restauranteService.deleteImagen(id);
    }

    // Crear carta para un restaurante
    @PostMapping("/{id}/carta")
    public void crearCarta(@PathVariable Integer id) {
        restauranteService.crearCarta(id);
    }

    // Reemplazar carta (borra la antigua y pone una nueva)
    @PutMapping("/{id}/carta")
    public void reemplazarCarta(@PathVariable Integer id) {
        restauranteService.reemplazarCarta(id);
    }

    // Eliminar carta
    @DeleteMapping("/{id}/carta")
    public void quitarCarta(@PathVariable Integer id) {
        restauranteService.quitarCarta(id);
    }

    // Obtener carta
    @GetMapping("/{id}/carta")
    public CartaDTO obtenerCarta(@PathVariable Integer id) {
        return restauranteService.obtenerCarta(id);
    }
}
