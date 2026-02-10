package com.example.demo.payload.request;

import lombok.Data;

@Data
public class ValoracionRequest {
    private Long citaId;

    // Nuevos campos
    private Integer tratoPersonal;
    private Integer desarrolloServicio;
    private Integer claridadComunicacion;
    private Integer limpieza;
    private Integer general;

    private String comentario;
}