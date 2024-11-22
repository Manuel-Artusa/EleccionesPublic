package ar.edu.utn.frc.tup.lc.iv.Clients.ApiClient;

import ar.edu.utn.frc.tup.lc.iv.Clients.*;
import ar.edu.utn.frc.tup.lc.iv.HttpClient.HttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EleccionApiClient {

    private final HttpClient httpClient;

    private final String url = "http://localhost:8080/";
    public AgrupacionRequest fetchAgrupacionById(Long id) {
        ResponseEntity<AgrupacionRequest> response =
                httpClient.get(url + "/agrupaciones/" + id, new ParameterizedTypeReference<AgrupacionRequest>() {});
        return response.getBody();
    }
    public List<CargoRequest> fetchCargo(){
        ResponseEntity<List<CargoRequest>> response =
                httpClient.get(url + "/cargos", new ParameterizedTypeReference<List<CargoRequest>>() {});
        return response.getBody();
    }
    public List<DistritoRequest> fetchDistrito(){
        ResponseEntity<List<DistritoRequest>> response =
                httpClient.get(url + "/distritos", new ParameterizedTypeReference<List<DistritoRequest>>() {});
        return response.getBody();
    }
    public List<ResultadoRequest> fetchResultadoByDistrictId(Long districtId){
        ResponseEntity<List<ResultadoRequest>> response =
                httpClient.get(url + "/resultados?districtId="+districtId, new ParameterizedTypeReference<List<ResultadoRequest>>() {});
        return response.getBody();
    }
    public List<SeccionRequest> fetchSeccion(){
        ResponseEntity<List<SeccionRequest>> response =
                httpClient.get(url + "/secciones", new ParameterizedTypeReference<List<SeccionRequest>>() {});
        return response.getBody();
    }
}
