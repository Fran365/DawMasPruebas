package WebAplicacionesDesarrollo.demo;

import WebAplicacionesDesarrollo.demo.dtos.*;
import WebAplicacionesDesarrollo.demo.entidades.Convocatoria;
import WebAplicacionesDesarrollo.demo.entidades.Materia;
import WebAplicacionesDesarrollo.demo.entidades.Prueba;
import WebAplicacionesDesarrollo.demo.entidades.Slot;
import WebAplicacionesDesarrollo.demo.repositorios.ConvocatoriaRepository;
import WebAplicacionesDesarrollo.demo.repositorios.PruebaRepository;
import WebAplicacionesDesarrollo.demo.repositorios.MateriaRepository;
import WebAplicacionesDesarrollo.demo.repositorios.SlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@DisplayName("En el servicio de pruebas")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PruebaTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Value(value="${local.server.port}")
    private int port;

    @Autowired
    private PruebaRepository pruebaRepository;

    @Autowired
    private MateriaRepository materiaRepository;

    @Autowired
    private ConvocatoriaRepository convocatoriaRepository;

    @Autowired
    private SlotRepository slotRepository;

    private final String jwtAntonio = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9"
            + ".eyJyb2xlIjpbIlZJQ0VSUkVDVE9SQURPIl0sInN1YiI6IjIiLCJpYXQiOjE3Nzc4MzQ2MjcsImV4cCI6MTg0MDkwNjYyN30"
            + ".r0TI0RGEIbvXkMS5NZBOmDyKveb1Pbt9DATV14xS3TUiiFNvgpI9nCPVT-J5LnQoTb00OzuZcp2UslZCkh78Eg";

    @BeforeEach
    public void limpiarBaseDeDatosGlobal() {
        pruebaRepository.deleteAll();
        slotRepository.deleteAll();
        materiaRepository.deleteAll();
        convocatoriaRepository.deleteAll();
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
        URI uri = uri(scheme, host,port, path);
        return RequestEntity.get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .build();
    }

    private RequestEntity<Void> delete(String scheme, String host, int port, String path, String token) {
        URI uri = uri(scheme, host,port, path);
        return RequestEntity.delete(uri)
                .header("Authorization", "Bearer " + token)
                .build();
    }

    private <T> RequestEntity<T> post(String scheme, String host, int port, String path, T object, String token) {
        URI uri = uri(scheme, host,port, path);
        return RequestEntity.post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .body(object);
    }

    private <T> RequestEntity<T> put(String scheme, String host, int port, String path, T object, String token) {
        URI uri = uri(scheme, host,port, path);
        return RequestEntity.put(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token) // <--- Aquí inyectamos el token
                .body(object);
    }

    private void compruebaCampos(Prueba expected, Prueba actual) {
        assertThat(actual.getSlot().getId()).isEqualTo(expected.getSlot().getId());
        assertThat(actual.getMateria().getId()).isEqualTo(expected.getMateria().getId());
        assertThat(actual.isEliminada()).isEqualTo(expected.isEliminada());
    }

    @Nested
    @DisplayName("cuando la base de datos está vacía")
    public class BaseDatosVacia{
        private Slot slotBD;
        private Materia materiaBD;
        //Todos estos metodos usan los mismoas datos los metemos en el before each para no duplicarlos
        @BeforeEach
        public void prepararDatos(){
            var convocatoriaDTO = ConvocatoriaNuevaDTO.builder().nombre("Convocatoria Jubio 2026")
                    .fechaInicio(java.time.LocalDateTime.of(2026, 6, 1, 9, 0))
                    .fechaFin(java.time.LocalDateTime.of(2026, 6, 30, 21, 0))
                    .build();
            var convocatoriaBD = convocatoriaRepository.save(ConvocatoriaMapper.toEntity(convocatoriaDTO));

            var slotNuevoDTO = SlotNuevoDTO.builder()
                    .inicio(java.time.LocalDateTime.of(2026, 6, 20, 10, 0))
                    .fin(java.time.LocalDateTime.of(2026, 6, 20, 12, 0))
                    .eliminado(false)
                    .build();
            Slot slotEntidad = SlotMapper.toEntity(slotNuevoDTO);
            slotEntidad.setConvocatoria(convocatoriaBD);
            slotBD = slotRepository.save(slotEntidad);

            var materiaNuevaDTO = MateriaNuevaDTO.builder()
                    .nombre("Desarrollo de Aplicaciones Web")
                    .build();
            materiaBD = materiaRepository.save(MateriaMapper.toEntity(materiaNuevaDTO));

        }

        private PruebaNuevaDTO generar(){
            return PruebaNuevaDTO.builder()
                    .slot(PruebaNuevaDTO.ReferenciaDTO.builder().id(slotBD.getId()).build())
                    .materia(PruebaNuevaDTO.ReferenciaDTO.builder().id(materiaBD.getId()).build())
                    .build();
        }
        //Esta es de CrearPrueba
        @Test
        @DisplayName("inserta correctamente una prueba")
        public void insertaPrueba() {

            //Construir PruebaDTO
            var pruebaNuevaDTO = generar();
            var peticion = post("http", "localhost", port, "/pruebas", pruebaNuevaDTO, jwtAntonio);
            var respuesta = restTemplate.exchange(peticion, Void.class);

            //Responde 201
            assertThat(respuesta.getStatusCode().value()).isEqualTo(201);

            // Cabecera aputna bien
            assertThat(Objects.requireNonNull(respuesta.getHeaders().get("Location")).get(0))
                    .startsWith("http://localhost:" + port + "/pruebas");

            //Guardado correcto
            List<Prueba> pruebasBD = pruebaRepository.findAll();
            assertThat(pruebasBD).hasSize(1);
            //ID de la URL coincida con el ID en BD
            assertThat(Objects.requireNonNull(respuesta.getHeaders().get("Location")).get(0))
                    .endsWith("/" + pruebasBD.get(0).getId());
            //Datos en BD consistentes
            compruebaCampos(pruebasBD.get(0), pruebasBD.get(0));

        }

        @Test
        @DisplayName("inserta una prueba con una autoriazcion incorrecta")
        public void insertaPruebaSinAutorizacion() {
            //Construir PruebaDTO
            var pruebaNuevaDTO = generar();

            String jwtVictoria = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJyb2xlIjpbXSwic3ViIjoiMyIsImlhdCI6MTc3NzgzNDgzMiwiZXhwIjoxODQwOTA2ODMyfQ.TgwNBRM_P2QLk8YVbiDIb7P0kOY_YHjyU7v9wkLuc_4aBBW7ZCTdkCMHFlN3wxN_x69WF8inKiZipHTVaDzmmg";
            var peticion = post("http", "localhost", port, "/pruebas", pruebaNuevaDTO, jwtVictoria);
            var respuesta = restTemplate.exchange(peticion, Void.class);

            //Responde 201
            assertThat(respuesta.getStatusCode().value()).isEqualTo(403);

            //Guardado correcto
            List<Prueba> pruebasBD = pruebaRepository.findAll();
            assertThat(pruebasBD).isEmpty();
        }

        @Test
        @DisplayName("falla al crear una prueba si el ID del slot no existe en la BD")
        public void crearPruebaSlotNoExiste() {
            var pruebaNuevaDTO = generar();
            pruebaNuevaDTO.setSlot(PruebaNuevaDTO.ReferenciaDTO.builder().id(9999L).build());
            var peticion = post("http", "localhost", port, "/pruebas", pruebaNuevaDTO, jwtAntonio);
            var respuesta = restTemplate.exchange(peticion, Void.class);

            // Verifica que salte el NoEncontradaException controlado
            assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
        }

        @Test
        @DisplayName("falla al crear una prueba si el ID de la materia no existe en la BD")
        public void crearPruebaMateriaNoExiste() {
            var pruebaNuevaDTO = generar();
            pruebaNuevaDTO.setMateria(PruebaNuevaDTO.ReferenciaDTO.builder().id(9999L).build());
            var peticion = post("http", "localhost", port, "/pruebas", pruebaNuevaDTO, jwtAntonio);
            var respuesta = restTemplate.exchange(peticion, Void.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
        }

        @Test
        @DisplayName("falla al insertar una prueba sin el objeto slot")
        public void insertaPruebaSinSlot() {
            // Enviamos el slot como NULL a propósito
            var pruebaNuevaDTO = generar();
            pruebaNuevaDTO.setSlot(null);

            var peticion = post("http", "localhost", port, "/pruebas", pruebaNuevaDTO, jwtAntonio);
            var respuesta = restTemplate.exchange(peticion, Void.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
        }

        @Test
        @DisplayName("falla al insertar una prueba con el id de slot a null")
        public void insertaPruebaConSlotIdNull() {
            var pruebaNuevaDTO = generar();
            pruebaNuevaDTO.setSlot(PruebaNuevaDTO.ReferenciaDTO.builder().id(null).build());
            var peticion = post("http", "localhost", port, "/pruebas", pruebaNuevaDTO, jwtAntonio);
            var respuesta = restTemplate.exchange(peticion, Void.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
        }

        @Test
        @DisplayName("falla al insertar una prueba sin el objeto materia")
        public void insertaPruebaSinMateria() {
            var pruebaNuevaDTO = generar();
            pruebaNuevaDTO.setMateria(null);
            var peticion = post("http", "localhost", port, "/pruebas", pruebaNuevaDTO, jwtAntonio);
            var respuesta = restTemplate.exchange(peticion, Void.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
        }

        @Test
        @DisplayName("falla al insertar una prueba con el id de materia a null")
        public void insertaPruebaConMateriaIdNull() {
            var pruebaNuevaDTO = generar();
            pruebaNuevaDTO.setMateria(PruebaNuevaDTO.ReferenciaDTO.builder().id(null).build());
            var peticion = post("http", "localhost", port, "/pruebas", pruebaNuevaDTO, jwtAntonio);
            var respuesta = restTemplate.exchange(peticion, Void.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
        }
    }

    @Nested
    @DisplayName("cuando la base de datos tiene informacion")
    public class BaseDatosAlgo{
        private Long materiaIdPrueba;
        private Long pruebaId;
        private Long slotIdPrueba;
        private Long convocatoriaIdPrueba;

        @BeforeEach
        public void llenarBD(){
            var convocatoria = new Convocatoria();
            convocatoria.setNombre("Convocatoria Ordinaria 2026");
            convocatoria.setFechaInicio(java.time.LocalDateTime.now());
            convocatoria.setFechaFin(java.time.LocalDateTime.now().plusMonths(1));
            convocatoria = convocatoriaRepository.save(convocatoria);

            this.convocatoriaIdPrueba = convocatoria.getIdConvocatoria();

            var materia = new Materia();
            materia.setNombre("Matemáticas");
            materia = materiaRepository.save(materia);
            this.materiaIdPrueba = materia.getId();

            var slot = new Slot();
            slot.setInicio(java.time.LocalDateTime.now());
            slot.setFin(java.time.LocalDateTime.now().plusHours(2));
            slot.setConvocatoria(convocatoria);
            slot = slotRepository.save(slot);

            this.slotIdPrueba = slot.getId();

            var prueba = new Prueba();
            prueba.setMateria(materia);
            prueba.setSlot(slot);
            prueba.setEliminada(false);

            prueba = pruebaRepository.save(prueba);
            this.pruebaId = prueba.getId();

            var pruebaEliminada = new Prueba();
            pruebaEliminada.setMateria(materia);
            pruebaEliminada.setSlot(slot);
            pruebaEliminada.setEliminada(true);
            pruebaRepository.save(pruebaEliminada);
        }

        //Para pruebaSlotConvocatoria
        @Test
        @DisplayName("Devuelve una lista de las pruebas de un slot especifico")
        public void devuelvePruebasDeUnSlotEspecifico(){
            URI uriCompleta = java.net.URI.create("http://localhost:" + port + "/pruebas?idSlot=" + slotIdPrueba);

            var peticion = RequestEntity.get(uriCompleta).header("Authorization", "Bearer " + jwtAntonio).build();

            var respuesta = restTemplate.exchange(peticion, List.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
            assertThat(respuesta.getBody()).isNotEmpty();
            assertThat(respuesta.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("Devuelve una lista de las pruebas de una convocatoria especifica")
        public void devuelvePruebasDeUnaConvocatoriaEspecifica(){
            URI uriCompleta = java.net.URI.create("http://localhost:" + port + "/pruebas?idConvocatoria=" + convocatoriaIdPrueba);

            var peticion = RequestEntity.get(uriCompleta).header("Authorization", "Bearer " + jwtAntonio).build();
            var respuesta = restTemplate.exchange(peticion, List.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
            assertThat(respuesta.getBody()).isNotEmpty();
            assertThat(respuesta.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("Devuelve una lista de todas las pruebas")
        public void devuelvePruebas() {
            var peticion = get("http", "localhost", port, "/pruebas", jwtAntonio);
            var respuesta = restTemplate.exchange(peticion, List.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
            assertThat(respuesta.getBody()).isNotEmpty();
            assertThat(respuesta.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("Devuelve 200 OK y la información de una prueba existente")
        public void informacionPruebaExistente() {
            var peticion = get("http", "localhost", port, "/pruebas/" + this.pruebaId, jwtAntonio);
            var respuesta = restTemplate.exchange(peticion, PruebaDTO.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
            assert respuesta.getBody() != null;
            assertThat(respuesta.getBody().getId()).isEqualTo(this.pruebaId);
        }

        @Test
        @DisplayName("Devuelve 404 Not Found si se busca una prueba inexistente")
        public void informacionPruebaNoExiste() {
            var peticion = get("http", "localhost", port, "/pruebas/9999", jwtAntonio);
            var respuesta = restTemplate.exchange(peticion, Void.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
        }


        @Test
        @DisplayName("Actualiza correctamente una prueba existente")
        public void actualizarPruebaExistente() {
            var pruebaNuevaDTO = PruebaNuevaDTO.builder()
                    .slot(PruebaNuevaDTO.ReferenciaDTO.builder().id(this.slotIdPrueba).build())
                    .materia(PruebaNuevaDTO.ReferenciaDTO.builder().id(this.materiaIdPrueba).build())
                    .build();

            var peticion = put("http", "localhost", port, "/pruebas/" + this.pruebaId, pruebaNuevaDTO, jwtAntonio);
            var respuesta = restTemplate.exchange(peticion, PruebaDTO.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
            assert respuesta.getBody() != null;
            assertThat(respuesta.getBody().getId()).isEqualTo(this.pruebaId);
        }


        @Test
        @DisplayName("Realiza un borrado lógico de una prueba")
        public void borrarPruebaExistente() {
            var peticion = delete("http", "localhost", port, "/pruebas/" + this.pruebaId, jwtAntonio);
            var respuesta = restTemplate.exchange(peticion, Void.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(200);

            // Verificamos directamente en el repositorio que el flag ha cambiado a true
            Prueba pruebaEnBd = pruebaRepository.findById(this.pruebaId).orElseThrow();
            assertThat(pruebaEnBd.isEliminada()).isTrue();
        }

        @Test
        @DisplayName("Falla al actualizar prueba con datos incompletos")
        public void actualizarPruebaDatosIncompletos() {
            var pruebaNuevaDTO = new PruebaNuevaDTO();
            var peticion = put("http", "localhost", port, "/pruebas/" + this.pruebaId, pruebaNuevaDTO, jwtAntonio);
            var respuesta = restTemplate.exchange(peticion, Void.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
        }

        @Test
        @DisplayName("Falla al actualizar prueba con ID de slot o materia inexistente")
        public void actualizarPruebaMateriaSlotInexistente() {
            var pruebaNuevaDTO = PruebaNuevaDTO.builder()
                    .slot(PruebaNuevaDTO.ReferenciaDTO.builder().id(9999L).build())
                    .materia(PruebaNuevaDTO.ReferenciaDTO.builder().id(9999L).build())
                    .build();
            var peticion = put("http", "localhost", port, "/pruebas/" + this.pruebaId, pruebaNuevaDTO, jwtAntonio);
            var respuesta = restTemplate.exchange(peticion, Void.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
        }

        @Test
        @DisplayName("Falla al actualizar prueba eliminada")
        public void actualizarPruebaEliminada() {
            //Ejecutamos el DELETE con el restTemplate
            var peticionDelete = delete("http", "localhost", port, "/pruebas/" + this.pruebaId, jwtAntonio);
            restTemplate.exchange(peticionDelete, Void.class);

            //Intentamos actualizar la prueba (que ya está borrada de verdad)
            var pruebaNuevaDTO = PruebaNuevaDTO.builder()
                    .slot(PruebaNuevaDTO.ReferenciaDTO.builder().id(this.slotIdPrueba).build())
                    .materia(PruebaNuevaDTO.ReferenciaDTO.builder().id(this.materiaIdPrueba).build())
                    .build();
            var peticionPut = put("http", "localhost", port, "/pruebas/" + this.pruebaId, pruebaNuevaDTO, jwtAntonio);
            var respuesta = restTemplate.exchange(peticionPut, Void.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
        }

        @Test
        @DisplayName("No devuelve pruebas eliminadas al listar")
        public void noDevuelvePruebasEliminadas() {
            //Ejecutamos el DELETE con el restTemplate
            var peticionDelete = delete("http", "localhost", port, "/pruebas/" + this.pruebaId, jwtAntonio);
            restTemplate.exchange(peticionDelete, Void.class);

            // Intentamos listar y verificamos que viene vacía
            var peticion = get("http", "localhost", port, "/pruebas", jwtAntonio);
            var respuesta = restTemplate.exchange(peticion, List.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
            assertThat(respuesta.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Falla al borrar prueba inexistente (cubre lambda de borrado)")
        public void borrarPruebaNoExiste() {
            var peticion = delete("http", "localhost", port, "/pruebas/9999", jwtAntonio);
            var respuesta = restTemplate.exchange(peticion, Void.class);
            assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
        }

        @Test
        @DisplayName("Falla al actualizar prueba con materia inexistente (cubre lambda de materia)")
        public void actualizarPruebaMateriaInexistente() {
            var pruebaNuevaDTO = PruebaNuevaDTO.builder()
                    .slot(PruebaNuevaDTO.ReferenciaDTO.builder().id(this.slotIdPrueba).build())
                    .materia(PruebaNuevaDTO.ReferenciaDTO.builder().id(9999L).build())
                    .build();
            var peticion = put("http", "localhost", port, "/pruebas/" + this.pruebaId, pruebaNuevaDTO, jwtAntonio);
            var respuesta = restTemplate.exchange(peticion, Void.class);
            assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
        }

        @Test
        @DisplayName("Falla al actualizar prueba con slot sin ID (cubre rama IF)")
        public void actualizarPruebaSlotSinId() {
            var pruebaNuevaDTO = PruebaNuevaDTO.builder()
                    .slot(new PruebaNuevaDTO.ReferenciaDTO())
                    .build();
            var peticion = put("http", "localhost", port, "/pruebas/" + this.pruebaId, pruebaNuevaDTO, jwtAntonio);
            var respuesta = restTemplate.exchange(peticion, Void.class);
            assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
        }

        @Test
        @DisplayName("Falla al actualizar prueba sin materia (cubre rama IF)")
        public void actualizarPruebaSinMateria() {
            var pruebaNuevaDTO = PruebaNuevaDTO.builder()
                    .slot(PruebaNuevaDTO.ReferenciaDTO.builder().id(this.slotIdPrueba).build())
                    .build();
            var peticion = put("http", "localhost", port, "/pruebas/" + this.pruebaId, pruebaNuevaDTO, jwtAntonio);
            var respuesta = restTemplate.exchange(peticion, Void.class);
            assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
        }

        @Test
        @DisplayName("Falla al actualizar prueba con materia sin ID (cubre rama IF)")
        public void actualizarPruebaMateriaSinId() {
            var pruebaNuevaDTO = PruebaNuevaDTO.builder()
                    .slot(PruebaNuevaDTO.ReferenciaDTO.builder().id(this.slotIdPrueba).build())
                    .materia(new PruebaNuevaDTO.ReferenciaDTO())
                    .build();
            var peticion = put("http", "localhost", port, "/pruebas/" + this.pruebaId, pruebaNuevaDTO, jwtAntonio);
            var respuesta = restTemplate.exchange(peticion, Void.class);
            assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
        }

        @Test
        @DisplayName("Filtra pruebas eliminadas al buscar por idSlot (cubre la última lambda)")
        public void filtraPruebasEliminadasPorIdSlot() {
            // 1. Borramos la prueba (eliminada = true)
            var peticionDelete = delete("http", "localhost", port, "/pruebas/" + this.pruebaId, jwtAntonio);
            restTemplate.exchange(peticionDelete, Void.class);

            // 2. Buscamos por idSlot. El repositorio devuelve la prueba borrada,
            // pero el Stream de Java (la lambda) la detecta y la filtra.
            var peticion = RequestEntity.get(URI.create("http://localhost:" + port + "/pruebas?idSlot=" + this.slotIdPrueba))
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + jwtAntonio)
                    .build();
            var respuesta = restTemplate.exchange(peticion, List.class);

            assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
            assertThat(respuesta.getBody()).isEmpty();
        }
    }
}