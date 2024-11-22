package ar.edu.utn.frc.tup.lc.iv.services;

import ar.edu.utn.frc.tup.lc.iv.Clients.AgrupacionRequest;
import ar.edu.utn.frc.tup.lc.iv.Clients.ApiClient.EleccionApiClient;
import ar.edu.utn.frc.tup.lc.iv.Clients.DistritoRequest;
import ar.edu.utn.frc.tup.lc.iv.Clients.ResultadoRequest;
import ar.edu.utn.frc.tup.lc.iv.Clients.SeccionRequest;
import ar.edu.utn.frc.tup.lc.iv.dtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class ResultService {
    private final EleccionApiClient eleccionApiClient;
    private final DistritoService distritoService;
    private final SeccionService seccionService;
    private Integer totalVotosNacionalesCache = null;

    public ResultadoDistritoDTO getResultadoByDistrito(Long distritoId) {
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

        // Calcular votos escrutados del distrito
        int votosEscrutados = resultadoRequestList.stream()
                .mapToInt(ResultadoRequest::getVotosCantidad)
                .sum();

        // Calcular porcentaje del padrón nacional (total votos por distrito)
        BigDecimal porcentajePadron = BigDecimal.valueOf(votosEscrutados)
                .divide(BigDecimal.valueOf(votosEscrutados), 4, RoundingMode.HALF_UP); // Aquí se usa total local, ajustar si es diferente

        // Agrupar resultados por nombre de agrupación o votos especiales
        Map<String, Integer> votosPorAgrupacion = new HashMap<>();

        resultadoRequestList.forEach(r -> {
            String nombreAgrupacion;

            if (r.getAgrupacionId() == 0) {
                // Usar votosTipo para votos especiales
                nombreAgrupacion = r.getVotosTipo();
            } else {
                // Obtener nombre de agrupación normal
                nombreAgrupacion = obtenerNombreAgrupacion(r.getAgrupacionId());
            }

            // Sumar votos al mapa
            votosPorAgrupacion.merge(nombreAgrupacion, r.getVotosCantidad(), Integer::sum);
        });

        // Convertir el mapa a lista y ordenar por cantidad de votos
        List<ResultadoAgrupacionDTO> resultadoAgrupaciones = votosPorAgrupacion.entrySet().stream()
                .map(entry -> ResultadoAgrupacionDTO.builder()
                        .nombre(entry.getKey())
                        .votos(entry.getValue())
                        .build())
                .sorted(Comparator.comparingInt(ResultadoAgrupacionDTO::getVotos).reversed())
                .collect(Collectors.toList());

        // Asignar posiciones y calcular porcentajes
        for (int i = 0; i < resultadoAgrupaciones.size(); i++) {
            ResultadoAgrupacionDTO resultado = resultadoAgrupaciones.get(i);
            resultado.setPosicion(i + 1);
            resultado.setPorcentaje(calcularPorcentajeString(resultado.getVotos(), votosEscrutados));
        }

        // Obtener agrupación ganadora
        String agrupacionGanadora = resultadoAgrupaciones.isEmpty() ? "" :
                resultadoAgrupaciones.get(0).getNombre();

        return ResultadoDistritoDTO.builder()
                .id(distrito.getId())
                .nombre(distrito.getNombre())
                .votosEscrutados(votosEscrutados)
                .porcentajePadronNacional(porcentajePadron)
                .secciones(secciones.stream()
                        .map(SeccionDTO::getNombre)
                        .collect(Collectors.toList()))
                .agrupacionGanadora(agrupacionGanadora)
                .resultadosAgrupaciones(resultadoAgrupaciones)
                .build();
    }
    public ResultadosDTO obtenerResultadosNacionales() {
        List<DistritoDTO> distritos = distritoService.getDistritos();
        List<ResultadoDistritoDTO> resultadosDistritos = new ArrayList<>();
        Map<String, Integer> votosTotalesPorAgrupacion = new HashMap<>();
        int totalVotosNacionales = 0;

        for (DistritoDTO distrito : distritos) {
            ResultadoDistritoDTO resultadoDistrito = calcularResultadosDistrito(distrito);
            resultadosDistritos.add(resultadoDistrito);

            totalVotosNacionales += resultadoDistrito.getVotosEscrutados();

            // Acumular votos nacionales por agrupación
            for (ResultadoAgrupacionDTO resultadoAgrupacion : resultadoDistrito.getResultadosAgrupaciones()) {
                votosTotalesPorAgrupacion.merge(
                        resultadoAgrupacion.getNombre(),
                        resultadoAgrupacion.getVotos(),
                        Integer::sum
                );
            }
        }

        // Crear lista de resultados nacionales ordenados
        List<ResultadoAgrupacionDTO> resultadosNacionales = votosTotalesPorAgrupacion.entrySet().stream()
                .map(entry -> ResultadoAgrupacionDTO.builder()
                        .nombre(entry.getKey())
                        .votos(entry.getValue())
                        .build())
                .sorted(Comparator.comparingInt(ResultadoAgrupacionDTO::getVotos).reversed())
                .collect(Collectors.toList());

        // Asignar posición y calcular porcentajes nacionales
        for (int i = 0; i < resultadosNacionales.size(); i++) {
            ResultadoAgrupacionDTO resultado = resultadosNacionales.get(i);
            resultado.setPosicion(i + 1);
            resultado.setPorcentaje(calcularPorcentajeString(resultado.getVotos(), totalVotosNacionales));
        }

        String agrupacionGanadora = resultadosNacionales.isEmpty() ? "" : resultadosNacionales.get(0).getNombre();

        return ResultadosDTO.builder()
                .distritos(distritos.stream().map(DistritoDTO::getNombre).collect(Collectors.toList()))
                .votosEscrutados(totalVotosNacionales)
                .agrupacionGanadora(agrupacionGanadora)
                .resultadosNacionales(resultadosNacionales)
                .resultadoDistritos(resultadosDistritos)
                .build();
    }

    private ResultadoDistritoDTO calcularResultadosDistrito(DistritoDTO distrito) {
        List<ResultadoRequest> resultados = eleccionApiClient.fetchResultadoByDistrictId(distrito.getId());
        int votosTotales = resultados.stream().mapToInt(ResultadoRequest::getVotosCantidad).sum();
        List<DistritoRequest> distritos = eleccionApiClient.fetchDistrito(); // Método para obtener la lista
        Map<String, Integer> votosPorAgrupacion = new HashMap<>();
        resultados.forEach(r -> {
            String nombreAgrupacion = r.getAgrupacionId() == 0 ? r.getVotosTipo() : obtenerNombreAgrupacion(r.getAgrupacionId());
            votosPorAgrupacion.merge(nombreAgrupacion, r.getVotosCantidad(), Integer::sum);
        });

        List<ResultadoAgrupacionDTO> resultadosAgrupaciones = votosPorAgrupacion.entrySet().stream()
                .map(entry -> ResultadoAgrupacionDTO.builder()
                        .nombre(entry.getKey())
                        .votos(entry.getValue())
                        .build())
                .sorted(Comparator.comparingInt(ResultadoAgrupacionDTO::getVotos).reversed())
                .collect(Collectors.toList());

        for (int i = 0; i < resultadosAgrupaciones.size(); i++) {
            ResultadoAgrupacionDTO resultado = resultadosAgrupaciones.get(i);
            resultado.setPosicion(i + 1);
            resultado.setPorcentaje(calcularPorcentajeString(resultado.getVotos(), votosTotales));
        }

        String agrupacionGanadora = resultadosAgrupaciones.isEmpty() ? "" : resultadosAgrupaciones.get(0).getNombre();
        // Obtener las secciones del distrito basado en los seccionId de los resultados
        List<String> secciones = resultados.stream()
                .map(r -> String.valueOf(r.getSeccionId()))  // Convertir seccionId a String
                .distinct()  // Eliminar duplicados
                .collect(Collectors.toList());
        List<String> seccionesNombre = secciones.stream()
                .map(id -> obtenerNombreSeccion(Long.valueOf(id)))  // Llamar al método obtenerNombreSeccion para cada seccionId
                .collect(Collectors.toList());

        return ResultadoDistritoDTO.builder()
                .id(distrito.getId())
                .nombre(distrito.getNombre())
                .secciones(seccionesNombre)
                .votosEscrutados(votosTotales)
                .porcentajePadronNacional(calcularPorcentaje(votosTotales, votosTotales)) // Ajustar si necesitas total nacional
                .agrupacionGanadora(agrupacionGanadora)
                .resultadosAgrupaciones(resultadosAgrupaciones)
                .build();
    }

    private int calcularTotalVotosNacionales() {
        if (totalVotosNacionalesCache == null) {
            totalVotosNacionalesCache = distritoService.getDistritos().stream()
                    .mapToInt(distrito -> eleccionApiClient.fetchResultadoByDistrictId(distrito.getId()).stream()
                            .mapToInt(ResultadoRequest::getVotosCantidad)
                            .sum())
                    .sum();
        }
        return totalVotosNacionalesCache;
    }

    private String obtenerNombreAgrupacion(Long agrupacionId) {
        AgrupacionRequest agrupacionRequest = eleccionApiClient.fetchAgrupacionById(agrupacionId);
        return agrupacionRequest.getAgrupacionNombre();
    }
    private String obtenerNombreSeccion(Long seccionId) {
        // Obtiene la lista de todas las secciones
        List<SeccionRequest> seccionRequest = eleccionApiClient.fetchSeccion();

        // Busca la sección que tiene el seccionId que coincide
        for (SeccionRequest seccion : seccionRequest) {
            if (seccion.getSeccionId().equals(seccionId)) {
                // Retorna el nombre de la sección encontrada
                return seccion.getSeccionNombre();
            }
        }

        // Si no se encuentra ninguna sección con ese ID, retorna un mensaje de error o null
        return "Sección no encontrada";  // O puedes retornar null, dependiendo del comportamiento que quieras
    }

    private String calcularPorcentajeString(int cantidad, int total) {
        if (total == 0) return "0.00 %";
        BigDecimal porcentaje = BigDecimal.valueOf(cantidad)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return porcentaje.setScale(2, RoundingMode.HALF_UP) + " %";
    }
    public BigDecimal calcularPorcentaje(int votosParciales, int votosTotales) {
        if (votosTotales == 0) {
            return BigDecimal.ZERO;  // Evitar división por cero
        }

        BigDecimal votosParcialesBD = BigDecimal.valueOf(votosParciales);
        BigDecimal votosTotalesBD = BigDecimal.valueOf(votosTotales);

        // Calculamos el porcentaje con dos decimales
        return votosParcialesBD.divide(votosTotalesBD, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }
}