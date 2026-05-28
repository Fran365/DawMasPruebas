package WebAplicacionesDesarrollo.demo.dtos;

import WebAplicacionesDesarrollo.demo.entidades.Prueba;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PruebaMapper {

    public static PruebaDTO toDTO(Prueba entidad) {
        if (entidad == null) {
            return null;
        }

        PruebaDTO dto = new PruebaDTO();
        dto.setId(entidad.getId());
        dto.setSlot(SlotMapper.toDTO(entidad.getSlot()));
        dto.setMateria(MateriaMapper.toDTO(entidad.getMateria()));
        dto.setEliminada(entidad.isEliminada());

        return dto;
    }

    public static List<PruebaDTO> toDTOList(List<Prueba> entidades) {
        return entidades.stream()
                .map(PruebaMapper::toDTO)
                .collect(Collectors.toList());
    }
}
