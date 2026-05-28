package WebAplicacionesDesarrollo.demo.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Convocatoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConvocatoria;

    @EqualsAndHashCode.Include
    @Column(nullable = false)
    private String nombre;

    @EqualsAndHashCode.Include
    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @EqualsAndHashCode.Include
    @Column(nullable = false)
    private LocalDateTime fechaFin;

    @OneToMany(mappedBy = "convocatoria", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Slot> slots;
}
