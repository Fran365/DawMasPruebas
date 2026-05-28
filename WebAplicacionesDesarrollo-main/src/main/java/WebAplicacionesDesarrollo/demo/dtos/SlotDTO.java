package WebAplicacionesDesarrollo.demo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO que representa un hueco temporal (slot) dentro de una convocatoria")
@Builder
public class SlotDTO {

    @Schema(description = "Identificador único del slot", example = "1")
    private Long id;

    @Schema(description = "Fecha y hora de inicio del slot", example = "2026-06-15T09:00:00")
    private LocalDateTime inicio;

    @Schema(description = "Fecha y hora de fin del slot", example = "2026-06-15T11:00:00")
    private LocalDateTime fin;

    @Schema(description = "Indica si el slot ha sido eliminado lógicamente", example = "false")
    private boolean eliminado;

    @Schema(description = "Información de la convocatoria a la que pertenece este slot", example = "1")
    private ConvocatoriaDTO convocatoria;
}