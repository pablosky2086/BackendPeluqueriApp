package com.example.demo.payload.request;

import lombok.Data;

@Data
public class BloqueoRequest {
    // Opción A: Bloqueo quirúrgico por ID
    private Long agendaId;

    // Opción B: Bloqueo masivo por rangos
    private Long grupoId;      // Opcional (si es null, bloquea todo el instituto)
    private String fechaInicio; // Formato ISO: "2025-12-20T08:00:00"
    private String fechaFin;

    private String motivo; // Obligatorio
}