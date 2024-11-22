package ar.edu.utn.frc.tup.lc.iv.controllers;

import ar.edu.utn.frc.tup.lc.iv.dtos.*;
import ar.edu.utn.frc.tup.lc.iv.services.CargoService;
import ar.edu.utn.frc.tup.lc.iv.services.DistritoService;
import ar.edu.utn.frc.tup.lc.iv.services.ResultService;
import ar.edu.utn.frc.tup.lc.iv.services.SeccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/elecciones")
public class EleccionesController {
    @Autowired
    private DistritoService distritoService;
    @Autowired
    private SeccionService seccionService;
    @Autowired
    private CargoService cargoService;
    @Autowired
    private ResultService resultService;

    @GetMapping("/distritos")
    public ResponseEntity<List<DistritoDTO>> getDistritos(@RequestParam(required = false, value = "distrito_nombre") String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return ResponseEntity.ok(distritoService.getDistritos());
        } else {
            return ResponseEntity.ok(distritoService.getDistritosByName(nombre));
        }

    }

    @GetMapping("/distritos/{id}")
    public ResponseEntity<DistritoDTO> getDistritoById(@PathVariable Long id) {
        return  ResponseEntity.ok(distritoService.getDistritosById(id));
    }

    @GetMapping("/distritos/{id}/secciones")
    public ResponseEntity<List<SeccionDTO>> getSeccionesByDistrito(@PathVariable Long id) {
        return ResponseEntity.ok(seccionService.getSeccionByDistrito(id));
    }

    @GetMapping("/distritos/{distritoId}/secciones/{seccionId}")
    public ResponseEntity<SeccionDTO> getSeccionByIdAndDistritoId(@PathVariable Long distritoId,
                                                                 @PathVariable Long seccionId) {
        return  ResponseEntity.ok(seccionService.getSeccionByIdAndDistrito(seccionId,distritoId));
    }

    @GetMapping("/distritos/{id}/cargos")
    public ResponseEntity<List<CargoDTO>> getCargosDistrito(@PathVariable Long id) {
        return ResponseEntity.ok(cargoService.getCargoDistrito(id));
    }

    @GetMapping("/distritos/{distritoId}/cargos/{cargoId}")
    public ResponseEntity<CargoDTO> getCargoByIdAndDistritoId(@PathVariable Long distritoId,
                                                               @PathVariable Long cargoId) {
        return ResponseEntity.ok(cargoService.getCargoDistritoAndCargo(distritoId,cargoId));
    }

    @GetMapping("/distritos/{id}/resultados")
    public ResponseEntity<ResultadoDistritoDTO> getResultadosByDistrito(@PathVariable Long id) {
        return ResponseEntity.ok(resultService.getResultadoByDistrito(id));
    }

    @GetMapping("/resultados")
    public ResponseEntity<ResultadosDTO> getResultadosGenerales() {
        return null;
    }

}
