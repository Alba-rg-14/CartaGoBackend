package com.cartaGo.cartaGo_backend.service;

import com.cartaGo.cartaGo_backend.dto.CartaDTO;
import com.cartaGo.cartaGo_backend.dto.PlatoDTO;
import com.cartaGo.cartaGo_backend.dto.RestauranteDTO;
import com.cartaGo.cartaGo_backend.dto.RestaurantePreviewDTO;
import com.cartaGo.cartaGo_backend.entity.Carta;
import com.cartaGo.cartaGo_backend.entity.Restaurante;
import com.cartaGo.cartaGo_backend.entity.Usuario;
import com.cartaGo.cartaGo_backend.repository.RestauranteRepository;
import com.cartaGo.cartaGo_backend.utils.GeoUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestauranteService {
    private final RestauranteRepository restauranteRepository;
    private final GeocodingService geocodingService;

    //Crear restaurante dado un usuario y nombre
    public Restaurante crearRestaurante(Usuario usuario, String nombre){
        Restaurante r = Restaurante.builder()
                .nombre(nombre)
                .usuario(usuario)
                .build();
        restauranteRepository.saveAndFlush(r);
        return r;
    }

    public List<RestauranteDTO> getAllRestaurantes() {
       return restauranteRepository.findAll().stream()
               .map(this::mapToDto) .toList();
    }

    public List<RestaurantePreviewDTO> getAllRestaurantesPreviewDTO(){
        return restauranteRepository.findAll().stream()
                .map(this::mapToPreviewDto).toList();

    }

    public RestauranteDTO getRestaurantesPreviewDTOByNombre(String name){

        return restauranteRepository.findByNombreContainingIgnoreCase(name)
                .map(this::mapToDto)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante no encontrado con nombre: " + name));

    }


    public RestaurantePreviewDTO getById(Integer id) {
        return restauranteRepository.findById(id).map(this::mapToPreviewDto).orElseThrow(() -> new EntityNotFoundException("Restaurante no encontrado con id: " + id));
    }

    public List<RestaurantePreviewDTO> getRestaurantesPreviewDTOAbiertos() {
        return restauranteRepository.findByEstado(Restaurante.Estado.abierto).stream()
                .map(this::mapToPreviewDto).toList();
    }

    public List<RestauranteDTO> findCercanos(double userLat, double userLon, double radioKm) {
        // 1) Bounding box para reducir candidatos
        double dLat = GeoUtils.deltaLatFor(radioKm);
        double dLon = GeoUtils.deltaLonFor(userLat, radioKm);

        double minLat = userLat - dLat;
        double maxLat = userLat + dLat;
        double minLon = userLon - dLon;
        double maxLon = userLon + dLon;

        // 2) Filtrar finamente por Haversine y mapear a DTO
        return restauranteRepository.findByLatBetweenAndLonBetween(minLat, maxLat, minLon, maxLon).stream()
                .filter(r -> GeoUtils.haversineKm(userLat, userLon, r.getLat(), r.getLon()) <= radioKm)
                .sorted((a, b) -> {
                    double da = GeoUtils.haversineKm(userLat, userLon, a.getLat(), a.getLon());
                    double db = GeoUtils.haversineKm(userLat, userLon, b.getLat(), b.getLon());
                    return Double.compare(da, db);
                })
                .map(this::mapToDto)
                .toList();
    }

    // por defecto radio 1km
    public List<RestauranteDTO> findCercanos1Km(double userLat, double userLon) {
        return findCercanos(userLat, userLon, 1.0);
    }

    public void setRestauranteUbicacion(Integer id, String dir) throws Exception{
        Double lat, lon;
        Restaurante r = restauranteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Restaurante no encontrado con id: " + id));
        r.setDireccion(dir);
        var res= geocodingService.buscarPorDireccion(dir);
        if (null != res){
            r.setLat(res.getLat() );
            r.setLon(res.getLon());
            restauranteRepository.saveAndFlush(r);
        }else {
            throw new Exception("No se ha encontrado la dirección:" + dir) ;
        }
    }

    public void setRestauranteCoor(Integer id, Double lat, Double lon){
        Restaurante r = restauranteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Restaurante no encontrado con id: " + id));
        r.setLat(lat);
        r.setLon(lon);
        var res = geocodingService.buscarPorCoordenadas(lat,lon);
        r.setDireccion(res.getDireccion());
        restauranteRepository.saveAndFlush(r);
    }


    private RestauranteDTO mapToDto(Restaurante r) {
        return RestauranteDTO.builder()
                .id(r.getId())
                .descripcion(r.getDescripcion())
                .nombre(r.getNombre())
                .imagen(r.getImagen())
                .estado(r.getEstado())
                .direccion(r.getDireccion())
                .lat(r.getLat())
                .lon(r.getLon())
                .build();
    }

    public RestaurantePreviewDTO mapToPreviewDto(Restaurante r){
        return RestaurantePreviewDTO.builder()
                .id(r.getId())
                .nombre(r.getNombre())
                .imagen(r.getImagen())
                .build();
    }

    public void cambiarEstado(Integer id) {
        Restaurante r = restauranteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Restaurante no encontrado con id: " + id));

        if((Restaurante.Estado.abierto).equals(r.getEstado())){
            r.setEstado(Restaurante.Estado.cerrado);
        }else if ((Restaurante.Estado.cerrado).equals(r.getEstado())){
            r.setEstado(Restaurante.Estado.abierto);
        }else{
            r.setEstado(Restaurante.Estado.abierto);
        }
        restauranteRepository.saveAndFlush(r);
    }

    public String setImagen(Integer id, String url) {
        String u = url == null ? null : url.trim();
        if (u == null || !u.startsWith("https://res.cloudinary.com/")) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "URL de imágen inválida");
        }
        Restaurante r = restauranteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Restaurante no encontrado con id: " + id));
        r.setImagen(u);
        restauranteRepository.saveAndFlush(r);
        return u;
    }

    public String getImagen(Integer id) {
        var r = restauranteRepository.findById(id).orElseThrow();
        return r.getImagen() == null ? "" : r.getImagen();
    }

    public void deleteImagen(Integer id) {
        var r = restauranteRepository.findById(id).orElseThrow();
        r.setImagen(null);
        restauranteRepository.saveAndFlush(r);
    }

    // Crear carta para un restaurante (1:1)
    @Transactional
    public void crearCarta(Integer id) {
        Restaurante r = restauranteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado con id: " + id));

        if (r.getCarta() != null) throw new IllegalStateException("Ya tiene carta");

        Carta c = new Carta();
        r.setCarta(c);              // enlaza ambos lados (método en la entity)
        // No hace falta llamar a save(c): cascade PERSIST desde Restaurante -> Carta
        // Tampoco a save(r) si r está managed; pero se puede por claridad:
        restauranteRepository.saveAndFlush(r);
    }

    // Reemplazar carta (borra la anterior por orphanRemoval y pone la nueva)
    @Transactional
    public void reemplazarCarta(Integer id) {
        Restaurante r = restauranteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado con id: " + id));
        Carta nueva = new Carta();

        r.setCarta(nueva); // el setter en la entity rompe el enlace antiguo y pone el nuevo
        restauranteRepository.saveAndFlush(r); // opcional si r sigue managed; explícito está bien
    }

    // Quitar (y borrar) la carta
    @Transactional
    public void quitarCarta(Integer id) {
        Restaurante r = restauranteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado con id: " + id));

        r.removeCarta();            // método en la entity
        restauranteRepository.saveAndFlush(r);    // al hacer flush/commit, orphanRemoval elimina la carta
    }

    @Transactional(readOnly = true)
    public CartaDTO obtenerCarta(Integer restauranteId) {
        Restaurante r = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado"));
        Carta c = r.getCarta();
        if (c == null) return null;

        return CartaDTO.builder()
                .id(c.getId())
                .restauranteId(r.getId())
                .platos(c.getPlatos().stream()
                        .map(p -> PlatoDTO.builder()
                                .id(p.getId())
                                .nombre(p.getNombre())
                                .descripcion(p.getDescripcion())
                                .precio(p.getPrecio())
                                .build())
                        .toList())
                .build();
    }

}
