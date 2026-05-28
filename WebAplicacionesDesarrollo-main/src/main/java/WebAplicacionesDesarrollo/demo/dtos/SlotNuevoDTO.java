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
@Schema(description = "Datos necesarios para crear un nuevo slot temporal")
@Builder
public class SlotNuevoDTO {

    @Schema(description = "Fecha y hora de inicio del slot",
            example = "2026-06-20T10:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime inicio;

    @Schema(description = "Fecha y hora de fin del slot",
            example = "2026-06-20T12:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime fin;

    @Schema(description = "Estado inicial de eliminación", example = "false")
    private boolean eliminado;

    // Arreglado: quitamos requiredMode = Schema.RequiredMode.REQUIRED a la convocatoria para que ya no sea obligatorio enviarla al hacer POST.
    // @Schema(description = "Objeto convocatoria al que se asignará este slot", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @Schema(description = "Objeto convocatoria al que se asignará este slot (Opcional, se asigna la actual por defecto)", example = "1")
    private ConvocatoriaDTO convocatoria;
}