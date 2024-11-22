package ar.edu.utn.frc.tup.lc.iv.services;

import ar.edu.utn.frc.tup.lc.iv.Clients.ApiClient.EleccionApiClient;
import ar.edu.utn.frc.tup.lc.iv.Clients.CargoRequest;
import ar.edu.utn.frc.tup.lc.iv.dtos.CargoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CargoService {

    private final EleccionApiClient eleccionApiClient;

    public List<CargoDTO> getCargoDistrito(Long distritoId){
        List<CargoRequest> cargoRequestList = eleccionApiClient.fetchCargo();
        List<CargoDTO> cargoDTOList = new ArrayList<>();

        for (CargoRequest cargoRequest : cargoRequestList){
            if (cargoRequest.getDistritoId().equals(distritoId)){
                CargoDTO cargoDTO = new CargoDTO();
                cargoDTO.setId(cargoRequest.getCargoId());
                cargoDTO.setNombre(cargoRequest.getCargoNombre());
                cargoDTOList.add(cargoDTO);
            }
        }
        return cargoDTOList;
    }
    public CargoDTO getCargoDistritoAndCargo(Long distritoId,Long cargoId){
        List<CargoRequest> cargoRequestList = eleccionApiClient.fetchCargo();


        for (CargoRequest cargoRequest : cargoRequestList){
            if (cargoRequest.getDistritoId().equals(distritoId)){
                if (cargoRequest.getCargoId().equals(cargoId)){
                    CargoDTO cargoDTO = new CargoDTO();
                    cargoDTO.setId(cargoRequest.getCargoId());
                    cargoDTO.setNombre(cargoRequest.getCargoNombre());
                    return  cargoDTO;
                }
            }
        }
        throw new RuntimeException("no se encontro el cargo");
    }
}
