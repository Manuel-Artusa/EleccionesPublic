package ar.edu.utn.frc.tup.lc.iv.services;

import ar.edu.utn.frc.tup.lc.iv.Clients.AgrupacionRequest;
import ar.edu.utn.frc.tup.lc.iv.Clients.ApiClient.EleccionApiClient;
import ar.edu.utn.frc.tup.lc.iv.Clients.ResultadoRequest;
import ar.edu.utn.frc.tup.lc.iv.dtos.DistritoDTO;
import ar.edu.utn.frc.tup.lc.iv.dtos.ResultadoAgrupacionDTO;
import ar.edu.utn.frc.tup.lc.iv.dtos.ResultadoDistritoDTO;
import ar.edu.utn.frc.tup.lc.iv.dtos.SeccionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResultService {
    private final EleccionApiClient eleccionApiClient;
    private final DistritoService distritoService;
    private final SeccionService seccionService;



    public ResultadoDistritoDTO getResultadoByDistrito(Long distritoId){

        // Obtener resultados del API por ID de distrito
        List<ResultadoRequest> resultadoRequestList = eleccionApiClient.fetchResultadoByDistrictId(distritoId);

        if (resultadoRequestList.isEmpty()) {
            throw new RuntimeException("No se encontraron resultados para el distrito con ID: " + distritoId);
        }

        // Obtener información del distrito
        DistritoDTO distrito = distritoService.getDistritosById(distritoId);

        if (distrito == null) {
            throw new RuntimeException("El distrito con ID: " + distritoId + " no existe.");
        }

        // Obtener las secciones asociadas al distrito
        List<SeccionDTO> secciones = seccionService.getSeccionByDistrito(distritoId);

        int votosEscrutados = resultadoRequestList.stream()
                .mapToInt( ResultadoRequest::getVotosCantidad)
                .sum();
        ResultadoRequest agrupacionGanadora = resultadoRequestList.stream()
                .max(Comparator.comparingInt(ResultadoRequest::getVotosCantidad))
                .orElse(null);

        if (agrupacionGanadora == null || agrupacionGanadora.getAgrupacionId() == null || agrupacionGanadora.getAgrupacionId() <= 0) {
            throw new RuntimeException("No se pudo determinar la agrupación ganadora para el distrito con ID: " + distritoId);
        }

        String nameAgrupacionGanadora = obternerNombreAgrupacion(agrupacionGanadora.getAgrupacionId());

        List<ResultadoAgrupacionDTO> resultadoAgrupaciones = resultadoRequestList.stream()
                .filter(r -> r.getAgrupacionId() != null && r.getAgrupacionId() > 0) // Validar IDs válidos
                .map(r -> {
                    String nombreAgrupacion = obternerNombreAgrupacion(r.getAgrupacionId());
                    return ResultadoAgrupacionDTO.builder()
                            .nombre(nombreAgrupacion)
                            .votos(r.getVotosCantidad())
                            .porcentaje(calcularPorcentaje(r.getVotosCantidad(), votosEscrutados))
                            .build();
                })
                .collect(Collectors.toList());

        List<String> nombreSecciones = secciones.stream()
                .map(SeccionDTO::getNombre)
                .collect(Collectors.toList());

        return ResultadoDistritoDTO.builder()
                .id(distrito.getId())
                .nombre(distrito.getNombre())
                .votosEscrutados(votosEscrutados)
                .porcentajePadronNacional(null)
                .secciones(nombreSecciones)
                .agrupacionGanadora(nameAgrupacionGanadora)
                .resultadosAgrupaciones(resultadoAgrupaciones)
                .build();

    }
    private String obternerNombreAgrupacion(Long agrupacionId) {
        AgrupacionRequest agrupacionRequest = eleccionApiClient.fetchAgrupacionById(agrupacionId);

        return agrupacionRequest.getAgrupacionNombre();
    }
    private String calcularPorcentaje(int cantidad, int total) {
        if (total == 0) return "0.00%";
        BigDecimal porcentaje = BigDecimal.valueOf(cantidad)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return porcentaje.setScale(2, RoundingMode.HALF_UP) + "%";
    }
}
