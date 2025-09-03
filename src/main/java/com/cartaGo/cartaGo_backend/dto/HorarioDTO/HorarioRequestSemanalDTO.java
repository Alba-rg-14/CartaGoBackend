package com.cartaGo.cartaGo_backend.dto.HorarioDTO;

import com.cartaGo.cartaGo_backend.dto.HorarioDTO.HorarioRequestDTO;
import lombok.*;

import java.util.List;

@Data @NoArgsConstructor
@AllArgsConstructor @Builder
public class HorarioRequestSemanalDTO {
    // lista de tramos; si un día no aparece aquí => ese día está CERRADO
    private List<HorarioRequestDTO> tramos;
}
