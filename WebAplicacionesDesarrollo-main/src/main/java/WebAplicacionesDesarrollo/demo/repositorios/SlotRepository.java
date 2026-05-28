package WebAplicacionesDesarrollo.demo.repositorios;

import WebAplicacionesDesarrollo.demo.entidades.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {
    // Buscamos por convocatoria
    List<Slot> findByConvocatoria_IdConvocatoria(Long idConvocatoria);

    // Obtenemos todos los slots que no están eliminados
    List<Slot> findByEliminadoFalse();

    // Obtenemos slots de una convocatoria que no estna eliminados
    List<Slot> findByConvocatoria_IdConvocatoriaAndEliminadoFalse(Long idConvocatoria);

    // Slots que ocurran en rango de fechas
    List<Slot> findByInicioBetweenAndEliminadoFalse(LocalDateTime inicio, LocalDateTime fin);
}
