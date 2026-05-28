package WebAplicacionesDesarrollo.demo.controladores;

import WebAplicacionesDesarrollo.demo.dtos.MateriaDTO;
import WebAplicacionesDesarrollo.demo.dtos.MateriaNuevaDTO;
import WebAplicacionesDesarrollo.demo.servicios.MateriaServicio;
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
@RequestMapping("/materias")
@CrossOrigin(origins = "*")
@Tag(name = "Materias", description = "Gestión de las materias del sistema")
public class MateriaControlador {

    private final MateriaServicio servicio;

    public MateriaControlador(MateriaServicio servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Obtener todas las materias", description = "Devuelve una lista de todas las materias que no han sido eliminadas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos de Vicerrectorado")
    })
    @GetMapping("")
    @PreAuthorize("hasAnyRole('VICERRECTORADO', 'ADMINISTRADOR')")
    public List<MateriaDTO> obtenerTodasMaterias() {
        return servicio.obtenerTodasMaterias();
    }

    @Operation(summary = "Obtener una materia por ID", description = "Devuelve los detalles de una materia específica mediante su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Materia encontrada"),
            @ApiResponse(responseCode = "404", description = "Materia no encontrada")
    })
    @GetMapping("/{idMateria}")
    @PreAuthorize("hasAnyRole('VICERRECTORADO', 'ADMINISTRADOR')")
    public ResponseEntity<MateriaDTO> obtenerMateria(@PathVariable Long idMateria) {
        return ResponseEntity.ok(servicio.obtenerMateriaPorId(idMateria));
    }

    @Operation(summary = "Crear una nueva materia", description = "Crea una materia y devuelve su ubicación en la cabecera Location.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Materia creada con éxito"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos")
    })
    @PostMapping("")
    @PreAuthorize("hasAnyRole('VICERRECTORADO', 'ADMINISTRADOR')")
    public ResponseEntity<MateriaDTO> crearMateria(@RequestBody MateriaNuevaDTO dto, UriComponentsBuilder uriBuilder) {
        MateriaDTO creada = servicio.crearMateria(dto);

        URI location = uriBuilder.path("/materias/{id}").buildAndExpand(creada.getId()).toUri();

        return ResponseEntity.created(location).body(creada);
    }

    @Operation(summary = "Actualizar una materia existente", description = "Modifica los datos de una materia existente mediante su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Materia actualizada con éxito"),
            @ApiResponse(responseCode = "404", description = "La materia a actualizar no existe"),
            @ApiResponse(responseCode = "400", description = "Datos de actualización inválidos")
    })
    @PutMapping("/{idMateria}")
    @PreAuthorize("hasAnyRole('VICERRECTORADO', 'ADMINISTRADOR')")
    public ResponseEntity<MateriaDTO> actualizarMateria(@PathVariable Long idMateria, @RequestBody MateriaNuevaDTO dto) {
        MateriaDTO actualizada = servicio.actualizarMateria(idMateria, dto);
        return ResponseEntity.ok(actualizada);
    }

    @Operation(summary = "Eliminación lógica de una materia", description = "Marca la materia como eliminada en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Materia marcada como eliminada"),
            @ApiResponse(responseCode = "404", description = "La materia no existe")
    })
    @DeleteMapping("/{idMateria}")
    @PreAuthorize("hasAnyRole('VICERRECTORADO', 'ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.OK)
    public void eliminarMateria(@PathVariable Long idMateria) {
        servicio.eliminarMateria(idMateria);
    }
}