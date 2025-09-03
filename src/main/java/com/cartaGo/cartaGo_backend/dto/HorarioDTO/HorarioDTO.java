package com.cartaGo.cartaGo_backend.dto.HorarioDTO;

import lombok.*;
import java.time.DayOfWeek;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class HorarioDTO {
    private Integer id;
    private DayOfWeek dia;
    private String apertura;
    private String cierre;
}
