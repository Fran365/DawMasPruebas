package WebAplicacionesDesarrollo.demo;

import WebAplicacionesDesarrollo.demo.dtos.ConvocatoriaDTO;
import WebAplicacionesDesarrollo.demo.dtos.SlotDTO;
import WebAplicacionesDesarrollo.demo.dtos.SlotNuevoDTO;
import WebAplicacionesDesarrollo.demo.entidades.Convocatoria;
import WebAplicacionesDesarrollo.demo.entidades.Slot;
import WebAplicacionesDesarrollo.demo.repositorios.ConvocatoriaRepository;
import WebAplicacionesDesarrollo.demo.repositorios.SlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestRestTemplate
@DisplayName("En el servicio de slots")
class SlotsTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private ConvocatoriaRepository convocatoriaRepository;

    @Value(value="${local.server.port}")
    private int port;

    // Token de Vicerrectorado
    private final String jwtAntonio = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9"
            + ".eyJyb2xlIjpbIlZJQ0VSUkVDVE9SQURPIl0sInN1YiI6IjIiLCJpYXQiOjE3Nzc4MzQ2MjcsImV4cCI6MTg0MDkwNjYyN30"
            + ".r0TI0RGEIbvXkMS5NZBOmDyKveb1Pbt9DATV14xS3TUiiFNvgpI9nCPVT-J5LnQoTb00OzuZcp2UslZCkh78Eg";

    @BeforeEach
    public void limpiarBaseDeDatosGlobal() {
        slotRepository.deleteAll();
        convocatoriaRepository.deleteAll();
    }

    private URI uri(String scheme, String host, int port, String ...paths) {
        UriBuilderFactory ubf = new DefaultUriBuilderFactory();
        UriBuilder ub = ubf.builder().scheme(scheme).host(host).port(port);
        for (String path: paths) { ub = ub.path(path); }
        return ub.build();
    }

    private RequestEntity<Void> get(String scheme, String host, int port, String path, String token) {
        URI uri = uri(scheme, host, port, path);
        return RequestEntity.get(uri).accept(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + token).build();
    }

    private <T> RequestEntity<T> post(String scheme, String host, int port, String path, T object, String token) {
        URI uri = uri(scheme, host, port, path);
        return RequestEntity.post(uri).contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + token).body(object);
    }

    private <T> RequestEntity<T> put(String scheme, String host, int port, String path, T object, String token) {
        URI uri = uri(scheme, host, port, path);
        return RequestEntity.put(uri).contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + token).body(object);
    }

    private RequestEntity<Void> delete(String scheme, String host, int port, String path, String token) {
        URI uri = uri(scheme, host, port, path);
        return RequestEntity.delete(uri).header("Authorization", "Bearer " + token).build();
    }

    @Nested
    @DisplayName("Cuando la base de datos está vacía")
    class BaseDatosVacia {

        // Arreglado: Nuevo test para comprobar que al hacer GET a /slots devuelve un código 200 OK con un cuerpo vacío ([]) en lugar de dar un error
        @Test
        @DisplayName("Devuelve lista vacía al obtener slots si no hay convocatorias en BD (GET /slots)")
        void obtieneListaVaciaSinConvocatorias() {
            var peticion = get("http", "localhost", port, "/slots", jwtAntonio);
            ResponseEntity<SlotDTO[]> respuesta = restTemplate.exchange(peticion, SlotDTO[].class);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(respuesta.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Falla al obtener un slot que no existe (GET /slots/{id})")
        void errorAlObtenerSlotInexistente() {
            var peticion = get("http", "localhost", port, "/slots/99", jwtAntonio);
            ResponseEntity<Void> respuesta = restTemplate.exchange(peticion, Void.class);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Falla al actualizar un slot que no existe (PUT /slots/{id})")
        void errorAlActualizarSlotInexistente() {
            var peticion = put("http", "localhost", port, "/slots/99", new SlotNuevoDTO(), jwtAntonio);
            ResponseEntity<Void> respuesta = restTemplate.exchange(peticion, Void.class);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Falla al borrar un slot que no existe (DELETE /slots/{id})")
        void errorAlBorrarSlotInexistente() {
            var peticion = delete("http", "localhost", port, "/slots/99", jwtAntonio);
            ResponseEntity<Void> respuesta = restTemplate.exchange(peticion, Void.class);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        // Arreglado: Eliminamos 3 test antiguos y nuevo test para comprobar que no requiere obligatoriamente que mandes la convocatoria, sino que el sistema falla porque no encuentra ninguna convocatoria activa a la que enganchar el slot por defecto.
        @Test
        @DisplayName("Falla al crear un slot porque no hay ninguna convocatoria en el sistema a la que asignarlo (POST /slots)")
        void errorAlCrearSlotSinConvocatoriaEnSistema() {
            SlotNuevoDTO nuevoSlot = SlotNuevoDTO.builder().inicio(LocalDateTime.now()).fin(LocalDateTime.now().plusHours(2)).build();
            var peticion = post("http", "localhost", port, "/slots", nuevoSlot, jwtAntonio);
            ResponseEntity<Void> respuesta = restTemplate.exchange(peticion, Void.class);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Cuando hay datos en la base de datos")
    class BaseDatosConDatos {

        private Convocatoria convocatoriaActiva;
        private Convocatoria convocatoriaAntigua;
        private Slot slotActivo;
        private Slot slotEliminado;

        @BeforeEach
        void prepararDatos() {
            Convocatoria c1 = new Convocatoria();
            c1.setNombre("Convocatoria Antigua");
            c1.setFechaInicio(LocalDateTime.now().minusDays(30).truncatedTo(ChronoUnit.SECONDS));
            c1.setFechaFin(LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.SECONDS));
            convocatoriaAntigua = convocatoriaRepository.save(c1);

            Convocatoria c2 = new Convocatoria();
            c2.setNombre("Convocatoria Activa");
            c2.setFechaInicio(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            c2.setFechaFin(LocalDateTime.now().plusDays(30).truncatedTo(ChronoUnit.SECONDS));
            convocatoriaActiva = convocatoriaRepository.save(c2);

            Slot s = new Slot();
            s.setInicio(LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS));
            s.setFin(LocalDateTime.now().plusDays(1).plusHours(2).truncatedTo(ChronoUnit.SECONDS));
            s.setConvocatoria(convocatoriaActiva);
            s.setEliminado(false);
            slotActivo = slotRepository.save(s);

            Slot s2 = new Slot();
            s2.setInicio(LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS));
            s2.setFin(LocalDateTime.now().plusDays(2).plusHours(2).truncatedTo(ChronoUnit.SECONDS));
            s2.setConvocatoria(convocatoriaActiva);
            s2.setEliminado(true);
            slotEliminado = slotRepository.save(s2);
        }

        // Arreglado: Para comprobar que endpoint /slots devuelve por defecto únicamente los slots de la convocatoria activa.
        @Test
        @DisplayName("Obtiene la lista de slots de la convocatoria actual por defecto (GET /slots)")
        void obtieneListaDeSlots() {
            Slot sViejo = new Slot();
            sViejo.setInicio(LocalDateTime.now().minusDays(10).truncatedTo(ChronoUnit.SECONDS));
            sViejo.setFin(LocalDateTime.now().minusDays(10).plusHours(2).truncatedTo(ChronoUnit.SECONDS));
            sViejo.setConvocatoria(convocatoriaAntigua);
            sViejo.setEliminado(false);
            Slot slotViejoGuardado = slotRepository.save(sViejo);

            var peticion = get("http", "localhost", port, "/slots", jwtAntonio);
            ResponseEntity<SlotDTO[]> respuesta = restTemplate.exchange(peticion, SlotDTO[].class);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);

            SlotDTO[] slots = respuesta.getBody();
            assertThat(slots).isNotNull();
            assertThat(slots).extracting(SlotDTO::getId).contains(slotActivo.getId());
            assertThat(slots).extracting(SlotDTO::getId).doesNotContain(slotEliminado.getId());
            assertThat(slots).extracting(SlotDTO::getId).doesNotContain(slotViejoGuardado.getId());
        }

        @Test
        @DisplayName("Obtiene la lista filtrando explícitamente por idConvocatoria (GET /slots?idConvocatoria=X)")
        void obtieneListaDeSlotsPorConvocatoria() {
            URI uriCompleta = URI.create("http://localhost:" + port + "/slots?idConvocatoria=" + convocatoriaActiva.getIdConvocatoria());
            var peticion = RequestEntity.get(uriCompleta).header("Authorization", "Bearer " + jwtAntonio).build();

            ResponseEntity<SlotDTO[]> respuesta = restTemplate.exchange(peticion, SlotDTO[].class);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(respuesta.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("Obtiene un slot por ID (GET /slots/{id})")
        void obtieneSlotPorId() {
            var peticion = get("http", "localhost", port, "/slots/" + slotActivo.getId(), jwtAntonio);
            ResponseEntity<SlotDTO> respuesta = restTemplate.exchange(peticion, SlotDTO.class);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);

            SlotDTO dtoResultado = respuesta.getBody();
            assertThat(dtoResultado).isNotNull();
            assertThat(dtoResultado.getId()).isEqualTo(slotActivo.getId());
        }

        // Arreglado: Eliminamos 1 test antiguo y nuevo test que envía un SlotNuevoDTO sin convocatoria. Y comprueba si asignamos a la convocatoria Activa.
        @Test
        @DisplayName("Crea un slot y se le asigna automáticamente la convocatoria actual del sistema (POST /slots)")
        void creaSlotAsignandoConvocatoriaActual() {
            SlotNuevoDTO nuevoSlot = SlotNuevoDTO.builder()
                    .inicio(LocalDateTime.now().plusDays(5).truncatedTo(ChronoUnit.SECONDS))
                    .fin(LocalDateTime.now().plusDays(5).plusHours(2).truncatedTo(ChronoUnit.SECONDS))
                    .eliminado(false)
                    .build();

            var peticion = post("http", "localhost", port, "/slots", nuevoSlot, jwtAntonio);
            ResponseEntity<SlotDTO> respuesta = restTemplate.exchange(peticion, SlotDTO.class);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            SlotDTO guardado = respuesta.getBody();
            assertThat(guardado).isNotNull();
            assertThat(guardado.getConvocatoria().getIdConvocatoria()).isEqualTo(convocatoriaActiva.getIdConvocatoria());
        }

        // Arreglado: Nuevo test que prueba que puedes mover un slot a otra convocatoria.
        @Test
        @DisplayName("Actualiza un slot cambiando su convocatoria (PUT /slots/{id})")
        void actualizaSlotCambiandoConvocatoria() {
            ConvocatoriaDTO convDTO = new ConvocatoriaDTO();
            convDTO.setIdConvocatoria(convocatoriaAntigua.getIdConvocatoria());

            SlotNuevoDTO actualizacion = SlotNuevoDTO.builder()
                    .inicio(LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS))
                    .fin(LocalDateTime.now().plusDays(10).plusHours(2).truncatedTo(ChronoUnit.SECONDS))
                    .convocatoria(convDTO)
                    .build();

            var peticion = put("http", "localhost", port, "/slots/" + slotActivo.getId(), actualizacion, jwtAntonio);
            ResponseEntity<SlotDTO> respuesta = restTemplate.exchange(peticion, SlotDTO.class);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);

            Slot modificado = slotRepository.findById(slotActivo.getId()).orElseThrow();
            assertThat(modificado.getInicio()).isEqualTo(actualizacion.getInicio());
            assertThat(modificado.getConvocatoria().getIdConvocatoria()).isEqualTo(convocatoriaAntigua.getIdConvocatoria());
        }

        // Arreglado: Nuevo test para que si el DTO de actualización no incluye una convocatoria, el backend ya no falla, sino que actualiza las fechas y mantiene la convocatoria que el slot ya tenía.
        @Test
        @DisplayName("Actualiza un slot modificando sus fechas pero manteniendo su convocatoria si no se envía (PUT /slots/{id})")
        void actualizaSlotManteniendoConvocatoria() {
            SlotNuevoDTO actualizacion = SlotNuevoDTO.builder()
                    .inicio(LocalDateTime.now().plusDays(20).truncatedTo(ChronoUnit.SECONDS))
                    .fin(LocalDateTime.now().plusDays(20).plusHours(2).truncatedTo(ChronoUnit.SECONDS))
                    .build();

            var peticion = put("http", "localhost", port, "/slots/" + slotActivo.getId(), actualizacion, jwtAntonio);
            ResponseEntity<SlotDTO> respuesta = restTemplate.exchange(peticion, SlotDTO.class);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);

            Slot modificado = slotRepository.findById(slotActivo.getId()).orElseThrow();
            assertThat(modificado.getInicio()).isEqualTo(actualizacion.getInicio());
            assertThat(modificado.getConvocatoria().getIdConvocatoria()).isEqualTo(convocatoriaActiva.getIdConvocatoria());
        }

        // Arreglado: Nuevo test que si se manda el objeto sin ID, verificamos que el servidor responde con 200 OK y que la convocatoria original se ha mantenido a salvo.
        @Test
        @DisplayName("Actualiza un slot manteniendo convocatoria si se envía el objeto pero sin ID (PUT /slots/{id})")
        void actualizaSlotConConvocatoriaIdNulo() {
            SlotNuevoDTO actualizacion = SlotNuevoDTO.builder()
                    .inicio(LocalDateTime.now().plusDays(15).truncatedTo(ChronoUnit.SECONDS))
                    .fin(LocalDateTime.now().plusDays(15).plusHours(2).truncatedTo(ChronoUnit.SECONDS))
                    .convocatoria(new ConvocatoriaDTO()) // Objeto instanciado, pero ID es null
                    .build();

            var peticion = put("http", "localhost", port, "/slots/" + slotActivo.getId(), actualizacion, jwtAntonio);
            ResponseEntity<SlotDTO> respuesta = restTemplate.exchange(peticion, SlotDTO.class);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);

            Slot modificado = slotRepository.findById(slotActivo.getId()).orElseThrow();
            assertThat(modificado.getInicio()).isEqualTo(actualizacion.getInicio());
            assertThat(modificado.getConvocatoria().getIdConvocatoria()).isEqualTo(convocatoriaActiva.getIdConvocatoria());
        }

        @Test
        @DisplayName("Falla al actualizar slot con convocatoria inválida (PUT /slots/{id})")
        void fallaActualizarConvocatoriaInvalida() {
            ConvocatoriaDTO convMala = new ConvocatoriaDTO();
            convMala.setIdConvocatoria(99L);
            SlotNuevoDTO actualizacion = SlotNuevoDTO.builder().convocatoria(convMala).build();

            var peticion = put("http", "localhost", port, "/slots/" + slotActivo.getId(), actualizacion, jwtAntonio);
            ResponseEntity<Void> respuesta = restTemplate.exchange(peticion, Void.class);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        // Arreglado: Quitamos el objeto ConvocatoriaDTO que antes se mandaba relleno.
        @Test
        @DisplayName("Falla al actualizar un slot que está marcado como eliminado (PUT /slots/{id})")
        void fallaActualizarSlotEliminado() {
            SlotNuevoDTO actualizacion = SlotNuevoDTO.builder().inicio(LocalDateTime.now()).fin(LocalDateTime.now().plusHours(2)).build();
            var peticion = put("http", "localhost", port, "/slots/" + slotEliminado.getId(), actualizacion, jwtAntonio);
            ResponseEntity<Void> respuesta = restTemplate.exchange(peticion, Void.class);
            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Elimina lógicamente un slot (DELETE /slots/{id})")
        void eliminaSlotCorrectamente() {
            var peticion = delete("http", "localhost", port, "/slots/" + slotActivo.getId(), jwtAntonio);
            ResponseEntity<Void> respuesta = restTemplate.exchange(peticion, Void.class);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);

            Slot slotModificado = slotRepository.findById(slotActivo.getId()).orElseThrow();
            assertThat(slotModificado.isEliminado()).isTrue();
        }

        @Test
        @DisplayName("Falla al eliminar un slot de una convocatoria antigua (DELETE /slots/{id})")
        void fallaEliminarSlotConvocatoriaAntigua() {
            Slot sViejo = new Slot();
            sViejo.setInicio(LocalDateTime.now().minusDays(10).truncatedTo(ChronoUnit.SECONDS));
            sViejo.setFin(LocalDateTime.now().minusDays(10).plusHours(2).truncatedTo(ChronoUnit.SECONDS));
            sViejo.setConvocatoria(convocatoriaAntigua);
            sViejo.setEliminado(false);
            Slot slotViejoGuardado = slotRepository.save(sViejo);

            var peticion = delete("http", "localhost", port, "/slots/" + slotViejoGuardado.getId(), jwtAntonio);
            ResponseEntity<Void> respuesta = restTemplate.exchange(peticion, Void.class);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

            Slot noModificado = slotRepository.findById(slotViejoGuardado.getId()).orElseThrow();
            assertThat(noModificado.isEliminado()).isFalse();
        }
    }
}