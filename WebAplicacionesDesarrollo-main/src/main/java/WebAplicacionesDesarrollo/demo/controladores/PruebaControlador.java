package WebAplicacionesDesarrollo.demo.controladores;

import WebAplicacionesDesarrollo.demo.dtos.PruebaDTO;
import WebAplicacionesDesarrollo.demo.dtos.PruebaNuevaDTO;
import WebAplicacionesDesarrollo.demo.excepcion.NoEncontradaException;
import WebAplicacionesDesarrollo.demo.servicios.PruebaServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/pruebas")
@CrossOrigin(origins = "*")
@Tag(name = "Pruebas", description = "Operaciones permitidas sobre la entidad Prueba")

public class PruebaControlador {
    private PruebaServicio servicio;

    public PruebaControlador(PruebaServicio servicio) { this.servicio = servicio;}

    @Operation(summary = "Listar pruebas", description = "Obtiene todas las pruebas. Permite filtrar opcionalmente por convocatoria y slot.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada con éxito"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos de Vicerrectorado")
    })
    @GetMapping("")
    @PreAuthorize("hasAnyRole('VICERRECTORADO', 'ADMINISTRADOR')")
    public ResponseEntity<List<PruebaDTO>> pruebaSlotConvocatoria(@RequestParam(required = false) Long idConvocatoria, @RequestParam(required = false) Long idSlot){
        List<PruebaDTO> lista = servicio.listarPruebas(idConvocatoria, idSlot);
        return ResponseEntity.ofNullable(lista);
    }

    @Operation(summary = "Crear prueba", description = "Crea una nueva prueba en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Prueba creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos")
    })
    @PostMapping("")
    @PreAuthorize("hasAnyRole('VICERRECTORADO', 'ADMINISTRADOR')")
    public ResponseEntity<PruebaDTO> crearPrueba(@RequestBody PruebaNuevaDTO prueba, UriComponentsBuilder uriBuilder){
        PruebaDTO nueva = servicio.crearPrueba(prueba);

        URI location = uriBuilder.path("/pruebas/{id}").buildAndExpand(nueva.getId()).toUri();

        return ResponseEntity.created(location).body(nueva);
    }

    @Operation(summary = "Obtener detalles de una prueba",
            description = "Devuelve la información detallada de una prueba específica mediante su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prueba encontrada con éxito"),
            @ApiResponse(responseCode = "404", description = "No se encontró ninguna prueba con el ID proporcionado")
    })
    @GetMapping("/{idPrueba}")
    @PreAuthorize("hasAnyRole('VICERRECTORADO', 'ADMINISTRADOR')")
    public ResponseEntity<PruebaDTO> informacionPrueba(@PathVariable  Long idPrueba){
        PruebaDTO prueba = servicio.obtenerPruebaPorId(idPrueba);
        return ResponseEntity.ofNullable(prueba);
    }

    @Operation(summary = "Actualizar una prueba",
            description = "Modifica los datos de una prueba existente. Requiere el ID en la URL y los nuevos datos en el cuerpo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prueba actualizada correctamente"),
            @ApiResponse(responseCode = "404", description = "No se pudo actualizar porque la prueba no existe"),
            @ApiResponse(responseCode = "400", description = "Datos de actualización inválidos")
    })
    @PutMapping("/{idPrueba}")
    @PreAuthorize("hasAnyRole('VICERRECTORADO', 'ADMINISTRADOR')")
    public ResponseEntity<PruebaDTO> actualizacionPrueba(@PathVariable  Long idPrueba, @RequestBody PruebaNuevaDTO dto){
        PruebaDTO actualizar =  servicio.actualizarPrueba(idPrueba, dto);
        return ResponseEntity.ofNullable(actualizar);
    }

    @Operation(summary = "Eliminar prueba", description = "Realiza el borrado lógico de una prueba.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prueba marcada como eliminada"),
            @ApiResponse(responseCode = "404", description = "La prueba no existe")
    })
    @DeleteMapping("/{idPrueba}")
    @PreAuthorize("hasAnyRole('VICERRECTORADO', 'ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.OK)
    public void borrarPrueba(@PathVariable  Long idPrueba){
        servicio.borradoLogicoPrueba(idPrueba);
    }

    @ExceptionHandler(NoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void noEncontrada(){
    }
}
