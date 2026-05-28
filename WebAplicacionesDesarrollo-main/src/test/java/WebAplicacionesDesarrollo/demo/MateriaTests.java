package WebAplicacionesDesarrollo.demo;

import WebAplicacionesDesarrollo.demo.dtos.MateriaDTO;
import WebAplicacionesDesarrollo.demo.dtos.MateriaNuevaDTO;
import WebAplicacionesDesarrollo.demo.entidades.Materia;
import WebAplicacionesDesarrollo.demo.repositorios.MateriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@DisplayName("En el servicio de materias")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MateriaTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Value(value="${local.server.port}")
    private int port;

    @Autowired
    private MateriaRepository materiaRepository;

    private final String jwtAntonio = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9"
            + ".eyJyb2xlIjpbIlZJQ0VSUkVDVE9SQURPIl0sInN1YiI6IjIiLCJpYXQiOjE3Nzc4MzQ2MjcsImV4cCI6MTg0MDkwNjYyN30"
            + ".r0TI0RGEIbvXkMS5NZBOmDyKveb1Pbt9DATV14xS3TUiiFNvgpI9nCPVT-J5LnQoTb00OzuZcp2UslZCkh78Eg";

    @BeforeEach
    void limpiarBD() {
        materiaRepository.deleteAll();
    }

    private URI uri(String scheme, String host, int port, String ...paths) {
        UriBuilderFactory ubf = new DefaultUriBuilderFactory();
        UriBuilder ub = ubf.builder()
                .scheme(scheme)
                .host(host).port(port);
        for (String path: paths) {
            ub = ub.path(path);
        }
        return ub.build();
    }

    private RequestEntity<Void> get(String scheme, String host, int port, String path, String token) {
        URI uri = uri(scheme, host, port, path);
        return RequestEntity.get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .build();
    }

    private <T> RequestEntity<T> post(String scheme, String host, int port, String path, T object, String token) {
        URI uri = uri(scheme, host, port, path);
        return RequestEntity.post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .body(object);
    }

    private <T> RequestEntity<T> put(String scheme, String host, int port, String path, T object, String token) {
        URI uri = uri(scheme, host, port, path);
        return RequestEntity.put(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .body(object);
    }

    private RequestEntity<Void> delete(String scheme, String host, int port, String path, String token) {
        URI uri = uri(scheme, host, port, path);
        return RequestEntity.delete(uri)
                .header("Authorization", "Bearer " + token)
                .build();
    }

    @Nested
    @DisplayName("cuando la base de datos está vacía")
    public class BaseDatosVacia {

        // CREAR MATERIA
        @Test
        @DisplayName("inserta correctamente una materia y devuelve 201")
        void crearMateria() {
            var materiaDTO = MateriaNuevaDTO.builder()
                    .nombre("Química")
                    .build();

            var peticion = post("http", "localhost", port, "/materias", materiaDTO, jwtAntonio);
            ResponseEntity<MateriaDTO> respuesta = restTemplate.exchange(peticion, MateriaDTO.class);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            List<Materia> materiasBD = materiaRepository.findAll();
            assertThat(materiasBD).hasSize(1);

            Long idGenerado = materiasBD.get(0).getId();
            String locationHeader = respuesta.getHeaders().getLocation().toString();
            assertThat(locationHeader).endsWith("/materias/" + idGenerado);
            assertThat(materiasBD.get(0).getNombre()).isEqualTo(materiaDTO.getNombre());
        }

        // OBTENER MATERIA POR ID
        @Test
        @DisplayName("devuelve 404 cuando se solicita una materia que no existe")
        void obtenerMateriaNoEncontrada() {
            var peticion = get("http", "localhost", port, "/materias/999", jwtAntonio);
            ResponseEntity<Object> respuesta = restTemplate.exchange(peticion, Object.class);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("cuando la base de datos tiene informacion")
    public class BaseDatosConDatos {

        private Materia materiaActiva;
        private Materia materiaEliminada;

        @BeforeEach
        void prepararDatos() {
            materiaRepository.deleteAll();

            var activa = new Materia();
            activa.setNombre("Matemáticas");
            activa.setEliminada(false);
            materiaActiva = materiaRepository.save(activa);

            var eliminada = new Materia();
            eliminada.setNombre("Historia");
            eliminada.setEliminada(true);
            materiaEliminada = materiaRepository.save(eliminada);
        }

        // OBTENER TODAS LAS MATERIAS
        @Test
        @DisplayName("devuelve una lista con solo las materias activas")
        void obtenerTodasMaterias() {
            var peticion = get("http", "localhost", port, "/materias", jwtAntonio);
            ResponseEntity<MateriaDTO[]> respuesta = restTemplate.exchange(peticion, MateriaDTO[].class);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);

            MateriaDTO[] listaDto = respuesta.getBody();
            assertThat(listaDto).isNotNull();
            assertThat(listaDto).hasSize(1);
            assertThat(listaDto)
                    .extracting(MateriaDTO::getNombre)
                    .containsExactly("Matemáticas");
        }

        // OBTENER MATERIA POR ID
        @Test
        @DisplayName("devuelve los detalles de una materia específica existente")
        void obtenerMateriaPorId() {
            var peticion = get("http", "localhost", port, "/materias/" + materiaActiva.getId(), jwtAntonio);
            ResponseEntity<MateriaDTO> respuesta = restTemplate.exchange(peticion, MateriaDTO.class);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(respuesta.getBody()).isNotNull();
            assertThat(respuesta.getBody().getNombre()).isEqualTo(materiaActiva.getNombre());
        }

        // ACTUALIZAR MATERIA
        @Test
        @DisplayName("modifica correctamente el nombre de una materia activa")
        void actualizarMateria() {
            var dtoActualizacion = MateriaNuevaDTO.builder()
                    .nombre("Matemáticas Avanzadas")
                    .build();

            var peticion = put("http", "localhost", port, "/materias/" + materiaActiva.getId(), dtoActualizacion, jwtAntonio);
            ResponseEntity<MateriaDTO> respuesta = restTemplate.exchange(peticion, MateriaDTO.class);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(respuesta.getBody()).isNotNull();
            assertThat(respuesta.getBody().getNombre()).isEqualTo("Matemáticas Avanzadas");

            Materia materiaBD = materiaRepository.findById(materiaActiva.getId()).orElse(null);
            assertThat(materiaBD).isNotNull();
            assertThat(materiaBD.getNombre()).isEqualTo("Matemáticas Avanzadas");
        }

        // ACTUALIZAR MATERIA
        @Test
        @DisplayName("devuelve 404 al intentar actualizar una materia eliminada logicamente")
        void actualizarMateriaEliminada() {
            var dtoActualizacion = MateriaNuevaDTO.builder()
                    .nombre("Historia Universal")
                    .build();

            var peticion = put("http", "localhost", port, "/materias/" + materiaEliminada.getId(), dtoActualizacion, jwtAntonio);
            ResponseEntity<Object> respuesta = restTemplate.exchange(peticion, Object.class);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("realiza un borrado lógico de una materia existente y devuelve 200")
        void eliminarMateriaExistente() {
            var peticion = delete("http", "localhost", port, "/materias/" + materiaActiva.getId(), jwtAntonio);
            ResponseEntity<Void> respuesta = restTemplate.exchange(peticion, Void.class);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);

            Materia materiaBD = materiaRepository.findById(materiaActiva.getId()).orElse(null);
            assertThat(materiaBD).isNotNull();
            assertThat(materiaBD.isEliminada()).isTrue();
        }

        @Test
        @DisplayName("devuelve 404 al intentar eliminar una materia que ya estaba eliminada o no existe")
        void eliminarMateriaInexistente() {
            var peticion = delete("http", "localhost", port, "/materias/9999", jwtAntonio);
            ResponseEntity<Void> respuesta = restTemplate.exchange(peticion, Void.class);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}