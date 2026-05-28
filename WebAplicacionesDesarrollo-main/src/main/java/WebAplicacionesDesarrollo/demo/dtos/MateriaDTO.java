package WebAplicacionesDesarrollo.demo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO que representa la información completa de una materia")
@Builder
public class MateriaDTO {
    @Schema(description = "Identificador único de la materia", example = "1")
    private Long id;

    @Schema(description = "Nombre oficial de la asignatura", example = "Desarrollo de Aplicaciones Web")
    private String nombre;

    @Schema(description = "Estado de borrado lógico (true si la materia no está activa)", example = "false")
    private boolean eliminada;
}