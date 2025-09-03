package com.cartaGo.cartaGo_backend.controller;

import com.cartaGo.cartaGo_backend.dto.CartaDTO.PlatosDTO.AlergenoDTO;
import com.cartaGo.cartaGo_backend.service.AlergenosService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AlergenosController {

    private final AlergenosService alergenosService;


    @GetMapping("/alergenos")
    public List<AlergenoDTO> listarAlergenos() {
        return alergenosService.listarAlergenos();
    }

}
