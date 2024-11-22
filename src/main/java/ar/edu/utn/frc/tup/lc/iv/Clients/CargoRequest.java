package ar.edu.utn.frc.tup.lc.iv.Clients;

import lombok.Data;

@Data
public class CargoRequest {
    private Long cargoId;
    private String cargoNombre;
    private Long distritoId;
}
