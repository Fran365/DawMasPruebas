package WebAplicacionesDesarrollo.demo.entidades;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter // Genera todos los getters automáticamente
@Setter // Genera todos los setters automáticamente
@NoArgsConstructor // Genera el constructor vacío exigido por JPA
@AllArgsConstructor // Genera un constructor con todos los parámetros
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Genera Equals y HashCode de forma segura
@ToString // Genera el ToString de forma segura


public class Prueba {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación N a 1: Muchas pruebas pueden asignarse a un mismo Slot de tiempo
    @EqualsAndHashCode.Include  // Usamos las relaciones que definen a la prueba
    @ManyToOne(optional = false)
    @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot;

    // Relación N a 1: Muchas pruebas a lo largo del tiempo pertenecen a la misma Materia
    @EqualsAndHashCode.Include
    @ManyToOne(optional = false)
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    // Controla si la prueba ha sido eliminada lógicamente
    @Column(nullable = false)
    private boolean eliminada;

}
