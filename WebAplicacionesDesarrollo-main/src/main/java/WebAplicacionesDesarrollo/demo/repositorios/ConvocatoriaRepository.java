package WebAplicacionesDesarrollo.demo.repositorios;

import WebAplicacionesDesarrollo.demo.entidades.Convocatoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface ConvocatoriaRepository extends JpaRepository<Convocatoria, Long> {

    // 1. Buscar por nombre
    Optional<Convocatoria> findByNombre(String nombre);

    // 2. Ver qué convocatorias están vigentes ahora mismo. Busca las que ya han empezado pero no han terminado
    List<Convocatoria> findTopByOrderByFechaInicioDesc();

    // 3. Buscar convocatorias que empiezan a partir de una fecha (próximas)
    List<Convocatoria> findByFechaInicioAfterOrderByFechaInicioAsc(LocalDateTime fecha);
}
