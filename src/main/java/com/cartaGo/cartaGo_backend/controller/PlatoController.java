package com.cartaGo.cartaGo_backend.controller;

import com.cartaGo.cartaGo_backend.dto.PlatoDTO;
import com.cartaGo.cartaGo_backend.dto.PlatoRequestDTO;
import com.cartaGo.cartaGo_backend.service.PlatoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PlatoController {

    private final PlatoService platoService;

    // Crear plato en una carta
    @PostMapping("/carta/{cartaId}/platos")
    public PlatoDTO crearPlato(@PathVariable Integer cartaId, @RequestBody PlatoRequestDTO req) {
        return platoService.crearPlato(cartaId, req);
    }

    // Listar platos de una carta (opcional filtrar por seccion)
    @GetMapping("/carta/{cartaId}/platos")
    public List<PlatoDTO> listarPlatos(@PathVariable Integer cartaId,
                                       @RequestParam(required = false) String seccion) {
        return platoService.listarPorCarta(cartaId, seccion);
    }

    // Obtener un plato
    @GetMapping("/platos/{platoId}")
    public PlatoDTO obtenerPlato(@PathVariable Integer platoId) {
        return platoService.obtenerPlato(platoId);
    }

    // Actualizar un plato
    @PutMapping("/platos/{platoId}")
    public PlatoDTO actualizarPlato(@PathVariable Integer platoId, @RequestBody PlatoRequestDTO req) {
        return platoService.actualizarPlato(platoId, req);
    }

    // Eliminar un plato
    @DeleteMapping("/platos/{platoId}")
    public void eliminarPlato(@PathVariable Integer platoId) {
        platoService.eliminarPlato(platoId);
    }

    // ===== Imagen (text/plain como en Restaurante) =====

    @PutMapping(path = "/platos/{platoId}/imagen", consumes = "text/plain", produces = "text/plain")
    public String setImagen(@PathVariable Integer platoId, @RequestBody String url) {
        return platoService.setImagen(platoId, url);
    }

    @GetMapping(path = "/platos/{platoId}/imagen", produces = "text/plain")
    public String getImagen(@PathVariable Integer platoId) {
        return platoService.getImagen(platoId);
    }

    @DeleteMapping("/platos/{platoId}/imagen")
    public void deleteImagen(@PathVariable Integer platoId) {
        platoService.deleteImagen(platoId);
    }
}
