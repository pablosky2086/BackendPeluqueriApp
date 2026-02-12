package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "agendas")
public class Agenda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    LocalDateTime horaInicio;
    LocalDateTime horaFin;
    String aula;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    Servicio servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", nullable = false)
    Grupo grupo;

    @OneToMany(mappedBy = "agenda")
    @JsonIgnore
    private List<Cita> citas;

    int sillas; // por defecto 10 determinado en el seed

    // --- NUEVOS CAMPOS ---
    @Column(nullable = false)
    private boolean bloqueada = false; // Por defecto FALSE (disponible)

    private String motivoBloqueo;
    // ---------------------

    public Agenda(LocalDateTime horaInicio, LocalDateTime horaFin, Servicio servicio, Grupo grupo, String aula, int sillas) {
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.servicio = servicio;
        this.grupo = grupo;
        this.aula = aula;
        this.sillas = sillas;
    }

    // Calcular los huecos dentro de la agenda
    public List<LocalDateTime> calcularHuecos() {
        int duracionServicio = servicio.getDuracion();
        List<LocalDateTime> huecos = new ArrayList<>();
        for (LocalDateTime i = horaInicio;i.isBefore(horaFin.minusMinutes(duracionServicio)); i = i.plusMinutes(duracionServicio)) {
            huecos.add(i);
        }
        return huecos;
    }

    // Ver si es disponible en una fecha y hora dada
    public boolean esDisponible(LocalDateTime fechaHora) {
        // 1. CHEQUEO DE BLOQUEO
        if (this.bloqueada) return false;

        // Comprobar si la fechaHora esta dentro del rango de la agenda
        if (fechaHora.isBefore(horaInicio) || fechaHora.isAfter(horaFin.minusMinutes(servicio.getDuracion()))) {
            return false;
        }
        // Comprobar si la fechaHora es anterior a la hora actual
        if (fechaHora.isBefore(LocalDateTime.now())) {
            return false;
        }
        // Comprobar el numero de citas ya existentes en esa hora de inicio
        long citasEnEseMomento = citas.stream()
                .filter(cita -> cita.getFechaHoraInicio().equals(fechaHora))
                .count();
        return citasEnEseMomento < sillas;
    }

    // Retornar una lista de horas disponibles con su estado
    public Map<LocalDateTime, Boolean> horasDisponiblesConEstado() {
        List<LocalDateTime> huecos = calcularHuecos();
        return huecos.stream()
                .collect(toMap(
                        hora -> hora,
                        hora -> esDisponible(hora)
                ));
    }
}