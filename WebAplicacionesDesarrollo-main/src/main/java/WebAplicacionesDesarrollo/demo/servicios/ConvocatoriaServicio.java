package WebAplicacionesDesarrollo.demo.servicios;

import WebAplicacionesDesarrollo.demo.dtos.ConvocatoriaDTO;
import WebAplicacionesDesarrollo.demo.dtos.ConvocatoriaMapper;
import WebAplicacionesDesarrollo.demo.dtos.ConvocatoriaNuevaDTO;
import WebAplicacionesDesarrollo.demo.entidades.Convocatoria;
import WebAplicacionesDesarrollo.demo.repositorios.ConvocatoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ConvocatoriaServicio {

    private ConvocatoriaRepository repositorio;

    public ConvocatoriaServicio(ConvocatoriaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<ConvocatoriaDTO> obtenerTodasConvocatorias() {
        return repositorio.findAll().stream()
                .map(ConvocatoriaMapper::toDTO)
                .toList();
    }

    public ConvocatoriaDTO crearConvocatoria(ConvocatoriaNuevaDTO nueva) {
        Convocatoria conv = new Convocatoria();
        conv.setNombre(nueva.getNombre());
        conv.setFechaInicio(nueva.getFechaInicio());
        conv.setFechaFin(nueva.getFechaFin());

        Convocatoria guardada = repositorio.save(conv);
        return ConvocatoriaMapper.toDTO(guardada);
    }

    public ConvocatoriaDTO obtenerConvocatoriaActual() {
        LocalDateTime ahora = LocalDateTime.now();
        // Usamos el metodo del repositorio para buscar las vigentes
        List<Convocatoria> vigentes = repositorio.findTopByOrderByFechaInicioDesc();

        // Si hay varias, devolvemos la primera. Si no, null
        return vigentes.isEmpty() ? null : ConvocatoriaMapper.toDTO(vigentes.get(0));
    }

}