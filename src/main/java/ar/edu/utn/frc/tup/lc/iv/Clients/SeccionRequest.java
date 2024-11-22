package ar.edu.utn.frc.tup.lc.iv.Clients;

import lombok.Data;

@Data
public class SeccionRequest {
    private Long seccionId;
    private String seccionNombre;
    private Long distritoId;
}
