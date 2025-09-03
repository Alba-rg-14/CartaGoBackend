package com.cartaGo.cartaGo_backend.dto.HorarioDTO;
import lombok.*;
import java.time.DayOfWeek;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class HorarioRequestDTO {
    private DayOfWeek dia;
    private String apertura;
    private String cierre;
}
