package ar.edu.utn.frc.tup.lc.iv.Clients;

import lombok.Data;

@Data
public class ResultadoRequest {
    private Long id;
    private Long distritoId;
    private Long seccionId;
    private Long agrupacionId;
    private Long cargoId;
    private String votosTipo;
    private int votosCantidad;
}
