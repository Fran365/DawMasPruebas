package WebAplicacionesDesarrollo.demo.dtos;
import WebAplicacionesDesarrollo.demo.entidades.Materia;

public class MateriaMapper {

    public static MateriaDTO toDTO(Materia entidad) {
        if (entidad == null) return null;

        return new MateriaDTO(
                entidad.getId(),
                entidad.getNombre(),
                entidad.isEliminada()
        );
    }

    public static Materia toEntity(MateriaNuevaDTO dto) {
        if (dto == null) return null;

        Materia entidad = new Materia();
        entidad.setNombre(dto.getNombre());
        entidad.setEliminada(false);
        return entidad;
    }
}