package ar.edu.utn.frc.tup.lc.iv.services;

import ar.edu.utn.frc.tup.lc.iv.Clients.ApiClient.EleccionApiClient;
import ar.edu.utn.frc.tup.lc.iv.Clients.SeccionRequest;
import ar.edu.utn.frc.tup.lc.iv.dtos.SeccionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SeccionService {
    private final EleccionApiClient eleccionApiClient;

    public List<SeccionDTO> getSeccionByDistrito(Long distritoId) {
        List<SeccionRequest> seccionRequestList = eleccionApiClient.fetchSeccion();

        List<SeccionDTO> seccionDTOList = new ArrayList<>();

        for (SeccionRequest seccionRequest : seccionRequestList){
            if (seccionRequest.getDistritoId().equals(distritoId)){
                SeccionDTO seccionDTO = new SeccionDTO();
                seccionDTO.setId(seccionRequest.getSeccionId());
                seccionDTO.setNombre(seccionRequest.getSeccionNombre());
                seccionDTOList.add(seccionDTO);
            }
        }
        return  seccionDTOList;
    }
    public SeccionDTO getSeccionByIdAndDistrito(Long seccionId,Long distritoId) {
        List<SeccionRequest> seccionRequestList = eleccionApiClient.fetchSeccion();

        List<SeccionDTO> seccionDTOList = new ArrayList<>();

        for (SeccionRequest seccionRequest : seccionRequestList){
            if (seccionRequest.getDistritoId().equals(distritoId)){
                if (seccionRequest.getSeccionId().equals(seccionId)){
                    SeccionDTO seccionDTO = new SeccionDTO();
                    seccionDTO.setId(seccionRequest.getSeccionId());
                    seccionDTO.setNombre(seccionRequest.getSeccionNombre());
                    return seccionDTO;
                }

            }
        }
        throw new RuntimeException("No se encotntro la seccion por distrito y seccion");
    }
}
