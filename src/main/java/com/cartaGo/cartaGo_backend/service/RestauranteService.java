package com.cartaGo.cartaGo_backend.service;

import com.cartaGo.cartaGo_backend.dto.RestauranteDTO;
import com.cartaGo.cartaGo_backend.dto.RestaurantePreviewDTO;
import com.cartaGo.cartaGo_backend.entity.Restaurante;
import com.cartaGo.cartaGo_backend.entity.Usuario;
import com.cartaGo.cartaGo_backend.repository.RestauranteRepository;
import com.cartaGo.cartaGo_backend.utils.GeoUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
