package ar.edu.utn.frc.tup.lc.iv.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SeccionDTO {

    private Long id;
    private String nombre;
}
