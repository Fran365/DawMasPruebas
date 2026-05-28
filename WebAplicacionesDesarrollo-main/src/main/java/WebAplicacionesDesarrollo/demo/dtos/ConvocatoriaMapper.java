package WebAplicacionesDesarrollo.demo.dtos;

import WebAplicacionesDesarrollo.demo.entidades.Convocatoria;

public class ConvocatoriaMapper {

    public static ConvocatoriaDTO toDTO(Convocatoria entidad) {
        if (entidad == null) return null;

        return new ConvocatoriaDTO(
                entidad.getIdConvocatoria(), // El ajuste de Long a Integer
                entidad.getNombre(),
                entidad.getFechaInicio(),
                entidad.getFechaFin()
        );
    }

    // Usamos ConvocatoriaNuevaDTO porque es el que viene del formulario de creación
    public static Convocatoria toEntity(ConvocatoriaNuevaDTO dto) {
        if (dto == null) return null;

        Convocatoria entidad = new Convocatoria();
        entidad.setNombre(dto.getNombre());
        entidad.setFechaInicio(dto.getFechaInicio());
        entidad.setFechaFin(dto.getFechaFin());
        return entidad;
    }
}