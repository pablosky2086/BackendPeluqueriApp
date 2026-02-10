package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "citas")
public class Cita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaHoraInicio;

    // Una cita pertenece a un cliente
    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // Una cita tiene una agenda
    @ManyToOne(optional = false)
    @JoinColumn(name = "agenda_id", nullable = false)
    private Agenda agenda;

    // En Cita.java
    @OneToOne(mappedBy = "cita", cascade = CascadeType.ALL)
    private Valoracion valoracion;

    public Cita(LocalDateTime inicio, Agenda agenda, Cliente cliente) {
        this.fechaHoraInicio = inicio;
        this.agenda = agenda;
        this.cliente = cliente;
    }
}
