package WebAplicacionesDesarrollo.demo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos requeridos para crear una nueva prueba vinculando un slot y una materia")
@Builder
public class PruebaNuevaDTO {

    @Schema(description = "Objeto Slot que define cuándo y dónde se realiza la prueba", requiredMode = Schema.RequiredMode.REQUIRED)
    private ReferenciaDTO slot;

    @Schema(description = "Objeto Materia que define la asignatura de la prueba", requiredMode = Schema.RequiredMode.REQUIRED)
    private ReferenciaDTO materia;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ReferenciaDTO {
        private Long id;
    }
}