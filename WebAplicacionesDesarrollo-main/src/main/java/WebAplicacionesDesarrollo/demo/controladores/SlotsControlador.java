package WebAplicacionesDesarrollo.demo.controladores;

import WebAplicacionesDesarrollo.demo.dtos.SlotDTO;
import WebAplicacionesDesarrollo.demo.dtos.SlotNuevoDTO;
import WebAplicacionesDesarrollo.demo.excepcion.NoEncontradaException;
import WebAplicacionesDesarrollo.demo.servicios.SlotServicio;
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
@RequestMapping("/slots")
@CrossOrigin(origins = "*")
@Tag(name = "Slots", description = "Gestión de slots")
public class SlotsControlador {

    private final SlotServicio servicio;

    public SlotsControlador(SlotServicio servicio) {
        this.servicio = servicio;
    }

    // GET /slots o GET /slots?idConvocatoria={id}
    @Operation(summary = "Obtener los slots de una convocatoria", description = "Devuelve una lista de todos los slots de una convocatoria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devolucion Correcta"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Convocatoria no encontrada")
    })@GetMapping("")
    @PreAuthorize("hasAnyAuthority('ROLE_VICERRECTORADO', 'ROLE_ADMINISTRADOR')")
    public ResponseEntity<List<SlotDTO>> obtenerSlots(@RequestParam(name = "idConvocatoria", required = false) Long convocatoria) {
        return ResponseEntity.ok(servicio.obtenerSlots(convocatoria));
    }

    // GET /slots/{idSlot}
    @Operation(summary = "Devuelve información de un slot concreto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devolucion correcta"),
            @ApiResponse(responseCode = "404", description = "Slot no encontrada")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_VICERRECTORADO', 'ROLE_ADMINISTRADOR')")
    public ResponseEntity<SlotDTO> obtenerSlotPorId(@PathVariable Long id) {
        // La excepción NoEncontradaException la lanza el servicio si no existe
        return ResponseEntity.ok(servicio.obtenerPorId(id));
    }

    // POST /slots
    @Operation(summary = "Crea un nuevo slot en la convocatoria actual. Devuelve el slot creado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Slot creado con éxito"),
            @ApiResponse(responseCode = "400", description = "Datos del slot inválidos"),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes")
    })
    @PostMapping("")
    @PreAuthorize("hasAnyRole('VICERRECTORADO', 'ADMINISTRADOR')")
    public ResponseEntity<SlotDTO> crearSlot(@RequestBody SlotNuevoDTO dto, UriComponentsBuilder uriBuilder) {
        SlotDTO creado = servicio.crearSlot(dto);
        URI location = uriBuilder.path("/slots/{id}").buildAndExpand(creado.getId()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    // PUT /slots/{idSlot}
    @Operation(summary = "Actualiza un slot concreto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actualizacion correcta"),
            @ApiResponse(responseCode = "400", description = "Datos de actualizacion erróneos"),
            @ApiResponse(responseCode = "404", description = "Slot no encontrado")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('VICERRECTORADO', 'ADMINISTRADOR')")
    public ResponseEntity<SlotDTO> actualizarSlot(@PathVariable Long id, @RequestBody SlotNuevoDTO dto) {
        SlotDTO actualizado = servicio.actualizarSlot(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    // DELETE /slots/{idSlot}
    @Operation(summary = "Eliminacion logica de un slot concreto. ", description = "Marca el slot como eliminada sin borrarla de la base de datos solo de la onvocatoria actual.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Eliminacion correcta"),
            @ApiResponse(responseCode = "404", description = "Slot no encontrado")
    })@DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('VICERRECTORADO', 'ADMINISTRADOR')")
    public ResponseEntity<Void> borrarSlot(@PathVariable Long id) {
        if (!servicio.borrarSlot(id)) {
            throw new NoEncontradaException("Slot no encontrado o no pertenece a la convocatoria actual");
        }
        return ResponseEntity.ok().build();
    }
}