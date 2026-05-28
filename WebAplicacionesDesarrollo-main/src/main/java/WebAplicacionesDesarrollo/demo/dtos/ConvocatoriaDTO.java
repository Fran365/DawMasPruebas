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
@Schema(description = "DTO que representa la información detallada de una convocatoria")
@Builder
public class ConvocatoriaDTO {
    @Schema(description = "Identificador único de la convocatoria", example = "1")
    private Long idConvocatoria;

    @Schema(description = "Nombre descriptivo de la convocatoria", example = "Convocatoria Ordinaria Junio 2026")
    private String nombre;

    @Schema(description = "Fecha y hora de inicio de la convocatoria", example = "2026-06-01T09:00:00")
    private LocalDateTime fechaInicio;

    @Schema(description = "Fecha y hora de finalización de la convocatoria", example = "2026-06-30T21:00:00")
    private LocalDateTime fechaFin;
}
