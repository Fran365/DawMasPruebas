package WebAplicacionesDesarrollo.demo.servicios;

import WebAplicacionesDesarrollo.demo.dtos.MateriaDTO;
import WebAplicacionesDesarrollo.demo.dtos.PruebaDTO;
import WebAplicacionesDesarrollo.demo.dtos.PruebaMapper;
import WebAplicacionesDesarrollo.demo.dtos.PruebaNuevaDTO;
import WebAplicacionesDesarrollo.demo.entidades.Materia;
import WebAplicacionesDesarrollo.demo.entidades.Prueba;
import WebAplicacionesDesarrollo.demo.entidades.Slot;
import WebAplicacionesDesarrollo.demo.excepcion.NoEncontradaException;
import WebAplicacionesDesarrollo.demo.repositorios.MateriaRepository;
import WebAplicacionesDesarrollo.demo.repositorios.PruebaRepository;
import WebAplicacionesDesarrollo.demo.repositorios.SlotRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PruebaServicio {

    private final PruebaRepository pruebaRepository;
    private final SlotRepository slotRepository;
    private final MateriaRepository materiaRepository;

    public PruebaServicio(PruebaRepository pruebaRepository, SlotRepository slotRepository, MateriaRepository materiaRepository) {
        this.pruebaRepository = pruebaRepository;
        this.slotRepository = slotRepository;
        this.materiaRepository = materiaRepository;
    }

    public List<PruebaDTO> listarPruebas(Long idConvocatoria, Long idSlot) {
        List<Prueba> pruebas;

        if (idSlot != null) {
            pruebas = pruebaRepository.findBySlot_Id(idSlot);
        } else if (idConvocatoria != null) {
            pruebas = pruebaRepository.findBySlot_Convocatoria_IdConvocatoria(idConvocatoria);
        } else {
            pruebas = pruebaRepository.findByEliminadaFalse();
        }

        return pruebas.stream()
                .filter(prueba -> !prueba.isEliminada())
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public PruebaDTO obtenerPruebaPorId(Long id) {
        Prueba prueba = pruebaRepository.findById(id)
                .orElseThrow(() -> new NoEncontradaException());
        return PruebaMapper.toDTO(prueba);
    }

    public PruebaDTO crearPrueba(PruebaNuevaDTO dto) {
        if (dto.getSlot() == null || dto.getSlot().getId() == null ||
                dto.getMateria() == null || dto.getMateria().getId() == null) {
            throw new NoEncontradaException("Faltan datos obligatorios");
        }

        Slot slot = slotRepository.findById(dto.getSlot().getId())
                .orElseThrow(() -> new NoEncontradaException("Slot no encontrado"));

        Materia materia = materiaRepository.findById(dto.getMateria().getId())
                .orElseThrow(() -> new NoEncontradaException("Materia no encontrada"));

        Prueba nuevaPrueba = new Prueba();
        nuevaPrueba.setSlot(slot);
        nuevaPrueba.setMateria(materia);
        nuevaPrueba.setEliminada(false);

        return PruebaMapper.toDTO(pruebaRepository.save(nuevaPrueba));
    }

    public PruebaDTO actualizarPrueba(Long id, PruebaNuevaDTO dto) {
        if (dto.getSlot() == null || dto.getSlot().getId() == null ||
                dto.getMateria() == null || dto.getMateria().getId() == null) {
            throw new NoEncontradaException("Faltan datos obligatorios");
        }

        Prueba pruebaExistente = pruebaRepository.findById(id)
                .filter(p -> !p.isEliminada())
                .orElseThrow(() -> new NoEncontradaException());

        Slot slot = slotRepository.findById(dto.getSlot().getId())
                .orElseThrow(() -> new NoEncontradaException("Slot no encontrado"));

        Materia materia = materiaRepository.findById(dto.getMateria().getId())
                .orElseThrow(() -> new NoEncontradaException("Materia no encontrada"));

        pruebaExistente.setSlot(slot);
        pruebaExistente.setMateria(materia);

        return PruebaMapper.toDTO(pruebaRepository.save(pruebaExistente));
    }

    public void borradoLogicoPrueba(Long id) {
        Prueba prueba = pruebaRepository.findById(id)
                .orElseThrow(() -> new NoEncontradaException());
        prueba.setEliminada(true);
        pruebaRepository.save(prueba);
    }

    private PruebaDTO convertirADTO(Prueba prueba) {
        PruebaDTO dto = new PruebaDTO();
        dto.setId(prueba.getId());
        dto.setEliminada(prueba.isEliminada());

        dto.setSlot(WebAplicacionesDesarrollo.demo.dtos.SlotMapper.toDTO(prueba.getSlot()));

        MateriaDTO materiaDTO = new MateriaDTO();
        materiaDTO.setId(prueba.getMateria().getId());
        materiaDTO.setNombre(prueba.getMateria().getNombre());
        materiaDTO.setEliminada(prueba.getMateria().isEliminada());
        dto.setMateria(materiaDTO);

        return dto;
    }
}