package WebAplicacionesDesarrollo.demo.controladores;

import WebAplicacionesDesarrollo.demo.dtos.ConvocatoriaDTO;
import WebAplicacionesDesarrollo.demo.dtos.ConvocatoriaNuevaDTO;
import WebAplicacionesDesarrollo.demo.servicios.ConvocatoriaServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/convocatorias")
@CrossOrigin(origins = "*")
@Tag(name = "Convocatorias", description = "Gestión de convocatorias")

public class ConvocatoriaControlador {

    private ConvocatoriaServicio servicio;

    public ConvocatoriaControlador(ConvocatoriaServicio servicio) { this.servicio = servicio;}

    @Operation(summary = "Obtener todas las convocatorias", description = "Devuelve una lista de todas las convocatorias.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos de Vicerrectorado")
    })
    @GetMapping("")
    @PreAuthorize("hasAnyRole('VICERRECTORADO', 'ADMINISTRADOR')")
    public List<ConvocatoriaDTO> obtenerTodasConvocatorias(){
        return servicio.obtenerTodasConvocatorias();
    }

    @Operation(summary = "Crea una nueva convocatoria y se convierte en actual.", description = "Devuelve la convocatoria creada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "La convocatoria se ha creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes")
    })
    @PostMapping("")
    @PreAuthorize("hasAnyRole('VICERRECTORADO', 'ADMINISTRADOR')")
    public ResponseEntity<ConvocatoriaDTO> crearConvocatoria(@RequestBody ConvocatoriaNuevaDTO dto, UriComponentsBuilder uriBuilder){
        ConvocatoriaDTO creada = servicio.crearConvocatoria(dto);
        URI location = uriBuilder.path("/convocatorias/{id}").buildAndExpand(creada.getIdConvocatoria()).toUri();
        return ResponseEntity.created(location).body(creada);

    }

    @Operation(summary = "Devuelve información de la convocatoria actual")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devolución correcta"),
            @ApiResponse(responseCode = "404", description = "Convocatoria no encontrada")
    })
    @GetMapping("/actual")
    @PreAuthorize("hasAnyRole('VICERRECTORADO', 'ADMINISTRADOR')")
    public ResponseEntity<ConvocatoriaDTO> obtenerConvocatoriaActual(){
        ConvocatoriaDTO actual = servicio.obtenerConvocatoriaActual();
        return ResponseEntity.ofNullable(actual);
    }
}
