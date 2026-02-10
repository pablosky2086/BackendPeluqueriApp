package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "valoraciones")
public class Valoracion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- NUEVOS CAMPOS ---
    @Column(nullable = false)
    private Integer tratoPersonal; // 1-5

    @Column(nullable = false)
    private Integer desarrolloServicio; // 1-5

    @Column(nullable = false)
    private Integer claridadComunicacion; // 1-5

    @Column(nullable = false)
    private Integer limpieza; // 1-5

    @Column(nullable = false)
    private Integer general; // 1-5
    // ---------------------

    @Column(length = 1000)
    private String comentario;

    private String fotoUrl;

    @OneToOne
    @JoinColumn(name = "cita_id", nullable = false, unique = true)
    @JsonIgnore
    private Cita cita;

    public String getUrlCompleta() {
        return (fotoUrl != null && !fotoUrl.isEmpty()) ? "/media/" + fotoUrl : null;
    }

    // EXTRA: Un método para obtener la media matemática real (útil para estadísticas)
    public Double getMediaMatematica() {
        if (tratoPersonal == null || desarrolloServicio == null) return 0.0;
        return (tratoPersonal + desarrolloServicio + claridadComunicacion + limpieza + general) / 5.0;
    }
}