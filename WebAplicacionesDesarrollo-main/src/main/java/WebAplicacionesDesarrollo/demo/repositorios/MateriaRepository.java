package WebAplicacionesDesarrollo.demo.repositorios;

import WebAplicacionesDesarrollo.demo.entidades.Materia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface MateriaRepository extends JpaRepository<Materia, Long> {

    // 1. Obtener todas las materias que NO están eliminadas (para mostrar listas)
    List<Materia> findByEliminadaFalse();

    // 2. Obtener una materia por su ID, asegurándonos de que no esté eliminada
    List<Materia> findByIdAndEliminadaFalse(Long id);

    // 3. Buscador: Buscar materias cuyo nombre contenga un texto (ignorando mayúsculas/minúsculas)
    List<Materia> findByNombreContainingIgnoreCase(String nombre);

    // 4. Validación: Comprobar si ya existe una materia con un nombre exacto (útil antes de crear una nueva)
    boolean existsByNombre(String nombre);

    // 5. Validación: Comprobar si existe una materia con ese nombre, pero que no esté eliminada
    boolean existsByNombreAndEliminadaFalse(String nombre);
}
