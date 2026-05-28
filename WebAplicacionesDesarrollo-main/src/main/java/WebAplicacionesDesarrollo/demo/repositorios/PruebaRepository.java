package WebAplicacionesDesarrollo.demo.repositorios;

import WebAplicacionesDesarrollo.demo.entidades.Prueba;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
@Repository
public interface PruebaRepository extends JpaRepository<Prueba, Long> {
    // Método para buscar pruebas por el ID del slot (para el GET /pruebas?idSlot=X)
    List<Prueba> findBySlot_Id(Long idSlot);

    // Método para buscar pruebas por el ID de la convocatoria (para el GET /pruebas?idConvocatoria=Y)
    @Query("SELECT p FROM Prueba p JOIN FETCH p.slot s WHERE s.convocatoria.idConvocatoria = :idConvocatoria")
    List<Prueba> findBySlot_Convocatoria_IdConvocatoria(@Param("idConvocatoria")Long idConvocatoria);

    // Método para buscar pruebas que no hayan sido eliminadas (para el borrado lógico)
    List<Prueba> findByEliminadaFalse();
}
