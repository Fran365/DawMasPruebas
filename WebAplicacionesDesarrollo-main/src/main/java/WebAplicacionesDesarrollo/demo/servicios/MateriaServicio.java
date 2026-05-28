package WebAplicacionesDesarrollo.demo.servicios;

import WebAplicacionesDesarrollo.demo.dtos.MateriaDTO;
import WebAplicacionesDesarrollo.demo.dtos.MateriaMapper;
import WebAplicacionesDesarrollo.demo.dtos.MateriaNuevaDTO;
import WebAplicacionesDesarrollo.demo.entidades.Materia;
import WebAplicacionesDesarrollo.demo.excepcion.NoEncontradaException;
import WebAplicacionesDesarrollo.demo.repositorios.MateriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MateriaServicio {

    private final MateriaRepository repositorio;

    public MateriaServicio(MateriaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<MateriaDTO> obtenerTodasMaterias() {
        return repositorio.findByEliminadaFalse().stream()
                .map(MateriaMapper::toDTO)
                .toList();
    }

    public MateriaDTO obtenerMateriaPorId(Long id) {
        Materia materia = repositorio.findById(id).orElseThrow(NoEncontradaException::new);
        return MateriaMapper.toDTO(materia);
    }

    public MateriaDTO crearMateria(MateriaNuevaDTO nueva) {
        Materia materia = MateriaMapper.toEntity(nueva);
        Materia guardada = repositorio.save(materia);
        return MateriaMapper.toDTO(guardada);
    }

    public MateriaDTO actualizarMateria(Long id, MateriaNuevaDTO dto) {
        Materia materia = repositorio.findById(id)
                .filter(m -> !m.isEliminada())
                .orElseThrow(NoEncontradaException::new);

        materia.setNombre(dto.getNombre());

        Materia actualizada = repositorio.save(materia);
        return MateriaMapper.toDTO(actualizada);
    }

    public void eliminarMateria(Long id) {
        Materia materia = repositorio.findById(id).orElseThrow(NoEncontradaException::new);
        materia.setEliminada(true);
        repositorio.save(materia);
    }
}