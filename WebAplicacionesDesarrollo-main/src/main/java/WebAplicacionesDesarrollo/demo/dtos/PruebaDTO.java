package WebAplicacionesDesarrollo.demo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import WebAplicacionesDesarrollo.demo.dtos.SlotDTO;
import WebAplicacionesDesarrollo.demo.dtos.MateriaDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor

@Schema(description = "DTO que representa la información completa de una prueba, incluyendo su slot y materia")
@Builder
public class PruebaDTO {

    @Schema(description = "Identificador único de la prueba", example = "1")
    private Long id;

    @Schema(description = "Información del hueco temporal (slot) asignado a la prueba")
    private SlotDTO slot;

    @Schema(description = "Información de la materia asociada a la prueba")
    private MateriaDTO materia;

    @Schema(description = "Estado de borrado lógico", example = "false")
    private boolean eliminada;
}
