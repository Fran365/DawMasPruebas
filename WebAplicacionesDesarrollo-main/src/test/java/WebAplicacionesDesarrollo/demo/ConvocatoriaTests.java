package WebAplicacionesDesarrollo.demo;

import WebAplicacionesDesarrollo.demo.dtos.ConvocatoriaDTO;
import WebAplicacionesDesarrollo.demo.dtos.ConvocatoriaNuevaDTO;
import WebAplicacionesDesarrollo.demo.entidades.Convocatoria;
import WebAplicacionesDesarrollo.demo.repositorios.ConvocatoriaRepository;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@DisplayName("En el servicio de convocatorias")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ConvocatoriaTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Value(value="${local.server.port}")
    private int port;

    @Autowired
    private ConvocatoriaRepository convocatoriaRepository;

    @BeforeEach
    void limpiarBD() {
        convocatoriaRepository.deleteAll();
    }

    // Token JWT de prueba (con permisos VICERRECTORADO)
    private final String jwtAntonio = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9"
            + ".eyJyb2xlIjpbIlZJQ0VSUkVDVE9SQURPIl0sInN1YiI6IjIiLCJpYXQiOjE3Nzc4MzQ2MjcsImV4cCI6MTg0MDkwNjYyN30"
            + ".r0TI0RGEIbvXkMS5NZBOmDyKveb1Pbt9DATV14xS3TUiiFNvgpI9nCPVT-J5LnQoTb00OzuZcp2UslZCkh78Eg";


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
        URI uri = uri(scheme, host,port, path);
        var peticion = RequestEntity.get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .build();
        return peticion;
    }


    private <T> RequestEntity<T> post(String scheme, String host, int port, String path, T object, String token) {
        URI uri = uri(scheme, host,port, path);
        var peticion = RequestEntity.post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .body(object);
        return peticion;
    }


    private void compruebaCampos(ConvocatoriaNuevaDTO expected, Convocatoria actual) {
        assertThat(actual.getNombre()).isEqualTo(expected.getNombre());
        assertThat(actual.getFechaInicio()).isEqualTo(expected.getFechaInicio());
        assertThat(actual.getFechaFin()).isEqualTo(expected.getFechaFin());
    }

    // --- TESTS ---

    @Nested
    @DisplayName("cuando la base de datos está vacía")
    public class BaseDatosVacia {


        @Test
        @DisplayName("inserta correctamente una convocatoria")
        void crearConvocatoria() {
            // Arrange: Preparamos la convocatoria a insertar
            var convocatoriaDTO = ConvocatoriaNuevaDTO.builder()
                    .nombre("Convocatoria Junio 2026")
                    .fechaInicio(LocalDateTime.of(2026, 6, 1, 9, 0))
                    .fechaFin(LocalDateTime.of(2026, 6, 30, 21, 0))
                    .build();

            // Act: Ejecutamos la petición POST
            var peticion = post("http", "localhost", port,"/convocatorias", convocatoriaDTO, jwtAntonio);
            ResponseEntity<ConvocatoriaDTO> respuesta = restTemplate.exchange(peticion, ConvocatoriaDTO.class);

            // Asserts
            // Verificamos el estado HTTP
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED); // 201 Created

            // Verificamos la cabecera Location (ahora con idConvocatoria)
            List<Convocatoria> convocatoriasBD = convocatoriaRepository.findAll();
            assertThat(convocatoriasBD).hasSize(1);

            Long idGenerado = convocatoriasBD.get(0).getIdConvocatoria();
            String locationHeader = respuesta.getHeaders().getLocation().toString();
            assertThat(locationHeader).endsWith("/convocatorias/" + idGenerado);

            // Verificar que se guardó fielmente en la Base de Datos
            compruebaCampos(convocatoriaDTO, convocatoriasBD.get(0));
        }

        @Test
        @DisplayName("devuelve null cuando se solicita la convocatoria actual")
        void obtenerConvocatoriaActualVacia() {
            // Arrange: Implícito por el BeforeEach
            // Construimos la petición a /convocatorias/actual
            var peticion = get("http", "localhost", port, "/convocatorias/actual", jwtAntonio);
            ResponseEntity<ConvocatoriaDTO> respuesta = restTemplate.exchange(peticion, ConvocatoriaDTO.class);

            // El controlador usa ResponseEntity.ofNullable(actual).
            // Si el servicio devuelve null, el controlador responderá un 404 Not Found
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

            // Comprobamos que el cuerpo esté vacío
            assertThat(respuesta.getBody()).isNull();
        }

        @Test
        @DisplayName("devuelve una lista vacía cuando no hay convocatorias")
        void obtenerTodasLasConvocatoriasVacia() {
            // Construimos la petición a /convocatorias
            var peticion = get("http", "localhost", port, "/convocatorias", jwtAntonio);
            ResponseEntity<ConvocatoriaDTO[]> respuesta = restTemplate.exchange(peticion, ConvocatoriaDTO[].class);

            // El estado HTTP debe ser 200 OK (la petición es correcta, solo que no hay datos)
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);

            // El cuerpo no debe ser nulo, debe ser un array existente
            ConvocatoriaDTO[] listaDto = respuesta.getBody();
            assertThat(listaDto).isNotNull();

            // El tamaño del array debe ser exactamente 0
            assertThat(listaDto).isEmpty();
            // También puedes poner: assertThat(listaDto).hasSize(0);
        }
    }

    @Nested
    @DisplayName("cuando la base de datos tiene informacion")
    public class BaseDatosConDatos {

        private Convocatoria convocatoriaMasAntigua;
        private Convocatoria convocatoriaMasReciente;


        @BeforeEach
        void prepararDatos() {
            // La BD se limpia en el BeforeEach global
            // Insertamos una convocatoria antigua (Enero 2026)
            var vieja = new Convocatoria();
            vieja.setNombre("Convocatoria Enero 2026");
            vieja.setFechaInicio(LocalDateTime.of(2026, 1, 1, 9, 0));
            vieja.setFechaFin(LocalDateTime.of(2026, 1, 31, 21, 0));
            convocatoriaMasAntigua = convocatoriaRepository.save(vieja);

            // Insertamos una convocatoria reciente (Junio 2026)
            var nueva = new Convocatoria();
            nueva.setNombre("Convocatoria Junio 2026");
            nueva.setFechaInicio(LocalDateTime.of(2026, 6, 1, 9, 0));
            nueva.setFechaFin(LocalDateTime.of(2026, 6, 30, 21, 0));
            convocatoriaMasReciente = convocatoriaRepository.save(nueva);
        }

        @Test
        @DisplayName("devuelve correctamente la convocatoria más reciente (actual)")
        void obtenerConvocatoriaActual() {

            var peticion = get("http", "localhost", port, "/convocatorias/actual", jwtAntonio);
            ResponseEntity<ConvocatoriaDTO> respuesta = restTemplate.exchange(peticion, ConvocatoriaDTO.class);

            // El estado HTTP debe ser OK (200)
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);

            // El cuerpo no debe ser nulo
            ConvocatoriaDTO dtoResultado = respuesta.getBody();
            assertThat(dtoResultado).isNotNull();

            // Comprobamos que el ID devuelto sea el de la más RECIENTE (Junio) y no el de la vieja
            assertThat(dtoResultado.getIdConvocatoria()).isEqualTo(convocatoriaMasReciente.getIdConvocatoria());
            assertThat(dtoResultado.getNombre()).isEqualTo(convocatoriaMasReciente.getNombre());
        }

        @Test
        @DisplayName("devuelve una lista con todas las convocatorias de la base de datos")
        void obtenerTodasLasConvocatorias() {

            var peticion = get("http", "localhost", port, "/convocatorias", jwtAntonio);
            ResponseEntity<ConvocatoriaDTO[]> respuesta = restTemplate.exchange(peticion, ConvocatoriaDTO[].class);

            // El estado debe ser 200 OK
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);

            // El cuerpo no debe ser nulo
            ConvocatoriaDTO[] listaDto = respuesta.getBody();
            assertThat(listaDto).isNotNull();

            // Como metimos 2 convocatorias en el @BeforeEach, la lista debe tener tamaño 2
            assertThat(listaDto).hasSize(2);

            // Verificamos que los nombres de las que están en la lista coinciden con las guardadas
            assertThat(listaDto)
                    .extracting(ConvocatoriaDTO::getNombre)
                    .containsExactlyInAnyOrder("Convocatoria Enero 2026", "Convocatoria Junio 2026");
        }
    }
}