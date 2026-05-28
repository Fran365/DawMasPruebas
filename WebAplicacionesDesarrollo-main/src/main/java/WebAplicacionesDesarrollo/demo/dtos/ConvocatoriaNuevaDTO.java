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

@Schema(description = "Datos necesarios para registrar una nueva convocatoria")
@Builder
public class ConvocatoriaNuevaDTO {

    @Schema(description = "Nombre de la convocatoria",
            example = "Convocatoria Extraordinaria Julio",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @Schema(description = "Fecha de inicio (Formato ISO: YYYY-MM-DDTHH:mm:ss)",
            example = "2026-07-01T08:30:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime fechaInicio;

    @Schema(description = "Fecha de fin (Formato ISO: YYYY-MM-DDTHH:mm:ss)",
            example = "2026-07-15T20:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime fechaFin;
}