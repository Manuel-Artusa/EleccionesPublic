package ar.edu.utn.frc.tup.lc.iv.services;

import ar.edu.utn.frc.tup.lc.iv.Clients.ApiClient.EleccionApiClient;
import ar.edu.utn.frc.tup.lc.iv.Clients.DistritoRequest;
import ar.edu.utn.frc.tup.lc.iv.dtos.DistritoDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.internal.bytebuddy.implementation.bytecode.Throw;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DistritoService {
    private final EleccionApiClient eleccionApiClient;

    public List<DistritoDTO> getDistritos(){
        List<DistritoDTO> distritoDTOList = new ArrayList<>();

        List<DistritoRequest> distritoRequests = eleccionApiClient.fetchDistrito();
        for(DistritoRequest distritoRequest: distritoRequests){
            DistritoDTO distritoDTO = new DistritoDTO();
            distritoDTO.setId(distritoRequest.getDistritoId());
            distritoDTO.setNombre(distritoRequest.getDistritoNombre());
            distritoDTOList.add(distritoDTO);
        }
        return distritoDTOList;
    }
    public List<DistritoDTO> getDistritosByName(String distritoName){

        List<DistritoDTO> distritoDTOList = new ArrayList<>();
        List<DistritoRequest> distritoRequests = eleccionApiClient.fetchDistrito();
        Optional<DistritoRequest> distritoOptional = distritoRequests.stream()
                .filter(distritoRequest -> distritoName.equals(distritoRequest.getDistritoNombre()))
                .findFirst();
        if (distritoOptional.isEmpty()) {
            throw new RuntimeException("No se encontró el distrito con el nombre: " + distritoName);
        }
        DistritoRequest distritoRequest = distritoOptional.get();
        DistritoDTO distritoDTO = new DistritoDTO();
        distritoDTO.setId(distritoRequest.getDistritoId());
        distritoDTO.setNombre(distritoRequest.getDistritoNombre());
        distritoDTOList.add(distritoDTO);

        return  distritoDTOList;

    }
    public DistritoDTO getDistritosById(Long id){

        List<DistritoRequest> distritoRequest = eleccionApiClient.fetchDistrito();
        Optional<DistritoRequest> optionalDistritoRequest = distritoRequest.stream()
                .filter(distritoRequest1 -> distritoRequest1.getDistritoId().equals(id))
                .findFirst();
        if (optionalDistritoRequest.isEmpty()){
            throw new RuntimeException("no se encontro el distrito con el id: "+ id);
        }
        DistritoDTO distritoDTO = new DistritoDTO();
        distritoDTO.setNombre(optionalDistritoRequest.get().getDistritoNombre());
        distritoDTO.setId(optionalDistritoRequest.get().getDistritoId());
        return distritoDTO;

    }

}
