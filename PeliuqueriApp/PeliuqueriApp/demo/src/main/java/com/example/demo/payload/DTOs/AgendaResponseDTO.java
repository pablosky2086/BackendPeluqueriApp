package com.example.demo.payload.DTOs;

import com.example.demo.model.Grupo;
import com.example.demo.model.Servicio;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class AgendaResponseDTO {

    private Long id;
    private LocalDateTime horaInicio;
    private LocalDateTime horaFin;
    private String aula;
    private int sillas;
    private boolean bloqueado;
    
    private Servicio servicio;
    private Grupo grupo;

    private Map<LocalDateTime, Boolean> horasDisponiblesEstado;
}
