package WebAplicacionesDesarrollo.demo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Objeto necesario para crear o modificar una materia")
@Builder
public class MateriaNuevaDTO {

    @Schema(description = "Nombre de la materia",
            example = "Sistemas de Gestión Empresarial",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;
}