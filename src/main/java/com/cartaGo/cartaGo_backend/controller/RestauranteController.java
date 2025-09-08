package com.cartaGo.cartaGo_backend.controller;

import com.cartaGo.cartaGo_backend.dto.CartaDTO.CartaDTO;
import com.cartaGo.cartaGo_backend.dto.CartaDTO.PlatosDTO.PlatoDTO;
import com.cartaGo.cartaGo_backend.dto.HorarioDTO.HorarioDTO;
import com.cartaGo.cartaGo_backend.dto.HorarioDTO.HorarioRequestDTO;
import com.cartaGo.cartaGo_backend.dto.HorarioDTO.HorarioRequestSemanalDTO;
import com.cartaGo.cartaGo_backend.dto.RestauranteDTO.RestauranteDTO;
import com.cartaGo.cartaGo_backend.dto.RestauranteDTO.RestaurantePreviewDTO;
import com.cartaGo.cartaGo_backend.dto.RestauranteDTO.RestauranteUpdateDTO;
import com.cartaGo.cartaGo_backend.repository.CartaRepository;
import com.cartaGo.cartaGo_backend.service.HorarioService;
import com.cartaGo.cartaGo_backend.service.RestauranteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/restaurante")
public class RestauranteController {

    private final RestauranteService restauranteService;
    private final HorarioService horarioService;
    private final CartaRepository cartaRepository;

    //-------------------------------------------------Getters de restaurante----------------------------------------------------------------------

    //GET all
    @GetMapping
    public List<RestauranteDTO> getAll(){
        return restauranteService.getAllRestaurantes();
    }

    //GET preview por ID
    @GetMapping("/{id}")
    public RestaurantePreviewDTO getById(@PathVariable Integer id){
        return restauranteService.getById(id);
    }

    //GET all preview
    @GetMapping("preview")
    public List<RestaurantePreviewDTO> getAllPreview(){
        return restauranteService.getAllRestaurantesPreviewDTO();
    }

    //GET preview por nombre
    @GetMapping("preview/{nombre}")
    public List<RestaurantePreviewDTO> getByNombre(@PathVariable String nombre){
       return restauranteService.getRestaurantesPreviewDTOByNombre(nombre);
    }

    //GET preview de los abiertos
    @GetMapping("preview/abiertos")
    public List<RestaurantePreviewDTO> getAbiertos(){
        return restauranteService.getRestaurantesPreviewDTOAbiertos();
    }

    @GetMapping("/{id}/info")
    public RestauranteDTO getRestauranteInfo(@PathVariable Integer id){
        return restauranteService.getRestauranteInfoById(id);
    }

    //PUT restaurante
    @PutMapping("/{id}")
    public RestauranteDTO actualizarRestaurante(@PathVariable Integer id,
                                                @RequestBody RestauranteUpdateDTO req) {
        return restauranteService.actualizarRestaurante(id, req);
    }

    // GET restaurantes en el radio de 1km
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

    //-------------------------------------------------Ubicación de un restaurante----------------------------------------------------------------------

    // Put ubicación a partir de dirección
    @PutMapping("/{id}/ubicacion")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setUbicacion(@PathVariable Integer id, @RequestParam String direccion) throws Exception {
        restauranteService.setRestauranteUbicacion(id, direccion);
    }

    // PUT ubicación a partir de coordenadas
    @PutMapping("/{id}/coordenadas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setCoordenadas(@PathVariable Integer id,
                               @RequestParam Double lat,
                               @RequestParam Double lon) {
        restauranteService.setRestauranteCoor(id, lat, lon);
    }


    //-------------------------------------------------Imagen de un restaurante----------------------------------------------------------------------

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

    //-------------------------------------------------Carta de un restaurante----------------------------------------------------------------------

    // POST carta para un restaurante
    @PostMapping("/{id}/carta")
    public void crearCarta(@PathVariable Integer id) {
        restauranteService.crearCarta(id);
    }

    // PUT carta (borra la antigua y pone una nueva)
    @PutMapping("/{id}/carta")
    public void reemplazarCarta(@PathVariable Integer id) {
        restauranteService.reemplazarCarta(id);
    }

    // DELETE carta
    @DeleteMapping("/{id}/carta")
    public void quitarCarta(@PathVariable Integer id) {
        restauranteService.quitarCarta(id);
    }

    // GET carta
    @GetMapping("/{id}/carta")
    public List<PlatoDTO> obtenerCarta(@PathVariable Integer id) {
        return restauranteService.obtenerCarta(id);
    }

    // GET /restaurante/{id}/carta/id  ->  200: <integer>, 404 si no hay carta
    @GetMapping("/{id}/carta/id")
    public ResponseEntity<Integer> getCartaId(@PathVariable Integer id) {
        return cartaRepository.findByRestauranteId(id)
                .map(c -> ResponseEntity.ok(c.getId()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El restaurante no tiene carta creada"));
    }

    //-------------------------------------------------Horario de un restaurante/ estado----------------------------------------------------------------------

    //PUT estado de un restaurante
    @PutMapping("/estado/{id}")
    public void cambiarEstado(@PathVariable Integer id){
        restauranteService.cambiarEstado(id);
    }

    // GET /restaurante/{restauranteId}/horarios
    @GetMapping("/{restauranteId}/horarios")
    public ResponseEntity<List<HorarioDTO>> listarHorarios(@PathVariable Integer restauranteId) {
        return ResponseEntity.ok(horarioService.listarHorarios(restauranteId));
    }

    // PUT /restaurante/{restauranteId}/horarios
    @PutMapping("/{restauranteId}/horarios")
    public ResponseEntity<List<HorarioDTO>> putHorariosSemanal(
            @PathVariable Integer restauranteId,
            @RequestBody HorarioRequestSemanalDTO req
    ) {
        return ResponseEntity.ok(horarioService.putSemanal(restauranteId, req));
    }

    // POST /restaurante/{restauranteId}/horarios
    @PostMapping("/{restauranteId}/horarios")
    public ResponseEntity<HorarioDTO> crearTramo(
            @PathVariable Integer restauranteId,
            @RequestBody HorarioRequestDTO slot
    ) {
        HorarioDTO dto = horarioService.crearTramo(restauranteId, slot);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // DELETE /restaurante/{restauranteId}/horarios/{horarioId}
    @DeleteMapping("/{restauranteId}/horarios/{horarioId}")
    public ResponseEntity<Void> borrarTramo(
            @PathVariable Integer restauranteId,
            @PathVariable Integer horarioId
    ) {
        horarioService.borrarTramo(horarioId);
        return ResponseEntity.noContent().build();
    }

    // POST /restaurante/{restauranteId}/horarios/append  (bulk sin reemplazar)
    @PostMapping("/{restauranteId}/horarios/append")
    public ResponseEntity<List<HorarioDTO>> appendHorariosSemanal(
            @PathVariable Integer restauranteId,
            @RequestBody HorarioRequestSemanalDTO req
    ) {
        List<HorarioDTO> creados = horarioService.appendSemanal(restauranteId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(creados);
    }

    // GET /restaurante/{id}/estado/auto
    @GetMapping("/{id}/estado/auto")
    public ResponseEntity<String> actualizarEstadoAuto(@PathVariable Integer id) {
        restauranteService.actualizarEstadoSegunHorario(id);
        return ResponseEntity.ok("Estado actualizado según horario");
    }


}
