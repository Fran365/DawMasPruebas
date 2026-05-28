package WebAplicacionesDesarrollo.demo.servicios;

import WebAplicacionesDesarrollo.demo.dtos.SlotDTO;
import WebAplicacionesDesarrollo.demo.dtos.SlotMapper;
import WebAplicacionesDesarrollo.demo.dtos.SlotNuevoDTO;
import WebAplicacionesDesarrollo.demo.entidades.Convocatoria;
import WebAplicacionesDesarrollo.demo.entidades.Slot;
import WebAplicacionesDesarrollo.demo.excepcion.NoEncontradaException;
import WebAplicacionesDesarrollo.demo.repositorios.ConvocatoriaRepository;
import WebAplicacionesDesarrollo.demo.repositorios.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SlotServicio {

    private final SlotRepository slotRepository;
    private final ConvocatoriaRepository convocatoriaRepository;

    public SlotServicio(SlotRepository slotRepository, ConvocatoriaRepository convocatoriaRepository) {
        this.slotRepository = slotRepository;
        this.convocatoriaRepository = convocatoriaRepository;
    }

    public List<SlotDTO> obtenerSlots(Long idConvocatoria) {
        // Arreglado: Si no se indica idConvocatoria se asume que se pregunta por la convocatoria actual, para GET /slots
        if (idConvocatoria == null) {
            List<Convocatoria> vigentes = convocatoriaRepository.findTopByOrderByFechaInicioDesc();
            if (vigentes.isEmpty()) {
                return List.of(); // Si no hay convocatoria actual, devolvemos vacío
            }
            idConvocatoria = vigentes.get(0).getIdConvocatoria();
        }

        List<Slot> slots = slotRepository.findByConvocatoria_IdConvocatoria(idConvocatoria);

        return slots.stream()
                .filter(slot -> !slot.isEliminado())
                .map(SlotMapper::toDTO)
                .toList();
        /*
        List<Slot> slots;
        if (idConvocatoria != null) {
            slots = slotRepository.findByConvocatoria_IdConvocatoria(idConvocatoria);
        } else {
            slots = slotRepository.findAll();
        }

        return slots.stream()
                .filter(slot -> !slot.isEliminado())
                .map(SlotMapper::toDTO)
                .toList();
         */
    }

    public SlotDTO obtenerPorId(Long id) {
        return slotRepository.findById(id)
                .map(SlotMapper::toDTO)
                .orElseThrow(() -> new NoEncontradaException("Slot no encontrado"));
    }

    public SlotDTO crearSlot(SlotNuevoDTO dto) {
        // Arreglado: En POST /slots para crear un nuevo slot en la convocatoria actual, ignoramos si el usuario manda la convocatoria o no, busca la actual.
        List<Convocatoria> vigentes = convocatoriaRepository.findTopByOrderByFechaInicioDesc();
        if (vigentes.isEmpty()) {
            throw new NoEncontradaException("No hay ninguna convocatoria actual registrada en el sistema.");
        }
        Convocatoria convocatoriaActual = vigentes.get(0);

        Slot slot = new Slot();
        slot.setInicio(dto.getInicio());
        slot.setFin(dto.getFin());
        slot.setEliminado(dto.isEliminado());
        slot.setConvocatoria(convocatoriaActual);

        return SlotMapper.toDTO(slotRepository.save(slot));

        /*
        if (dto.getConvocatoria() == null || dto.getConvocatoria().getIdConvocatoria() == null) {
            throw new NoEncontradaException("La convocatoria indicada no existe (faltan datos)");
        }

        Long idConv = dto.getConvocatoria().getIdConvocatoria();

        Convocatoria convocatoria = convocatoriaRepository.findById(idConv)
                .orElseThrow(() -> new NoEncontradaException("La convocatoria indicada no existe"));

        Slot slot = new Slot();
        slot.setInicio(dto.getInicio());
        slot.setFin(dto.getFin());
        slot.setEliminado(dto.isEliminado());
        slot.setConvocatoria(convocatoria);

        return SlotMapper.toDTO(slotRepository.save(slot));
         */
    }

    public SlotDTO actualizarSlot(Long id, SlotNuevoDTO dto) {
        // // Arreglado: Para PUT no se rompa ahora que  idConvocatoria es opcional. Ahora si me envía datos de la convocatoria en el PUT la actualizo. Si no le dejo al slot la convocatoria que ya tenía asignada antes.
        Slot slot = slotRepository.findById(id)
                .filter(s -> !s.isEliminado())
                .orElseThrow(() -> new NoEncontradaException("El slot no existe"));

        if (dto.getConvocatoria() != null && dto.getConvocatoria().getIdConvocatoria() != null) {
            Long idConv = dto.getConvocatoria().getIdConvocatoria();
            Convocatoria convocatoria = convocatoriaRepository.findById(idConv)
                    .orElseThrow(() -> new NoEncontradaException("La convocatoria indicada no existe"));
            slot.setConvocatoria(convocatoria);
        }

        slot.setInicio(dto.getInicio());
        slot.setFin(dto.getFin());

        return SlotMapper.toDTO(slotRepository.save(slot));

        /*
        Slot slot = slotRepository.findById(id)
                .filter(s -> !s.isEliminado())
                .orElseThrow(() -> new NoEncontradaException("El slot no existe"));

        if (dto.getConvocatoria() == null || dto.getConvocatoria().getIdConvocatoria() == null) {
            throw new NoEncontradaException("La convocatoria indicada no existe (faltan datos)");
        }

        Long idConv = dto.getConvocatoria().getIdConvocatoria();

        Convocatoria convocatoria = convocatoriaRepository.findById(idConv)
                .orElseThrow(() -> new NoEncontradaException("La convocatoria indicada no existe"));

        slot.setInicio(dto.getInicio());
        slot.setFin(dto.getFin());
        slot.setConvocatoria(convocatoria);

        return SlotMapper.toDTO(slotRepository.save(slot));
         */
    }

    public boolean borrarSlot(Long id) {
        return slotRepository.findById(id).map(slot -> {
            List<Convocatoria> vigentes = convocatoriaRepository.findTopByOrderByFechaInicioDesc();

            Convocatoria actual = vigentes.get(0);

            if (!slot.getConvocatoria().getIdConvocatoria().equals(actual.getIdConvocatoria())) {
                return false;
            }

            slot.setEliminado(true);
            slotRepository.save(slot);
            return true;
        }).orElse(false);
    }
}