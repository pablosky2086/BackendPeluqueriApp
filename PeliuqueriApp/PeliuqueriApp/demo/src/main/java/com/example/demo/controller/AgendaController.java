package com.example.demo.controller;

import com.example.demo.model.Agenda;
import com.example.demo.payload.DTOs.AgendaResponseDTO;
import com.example.demo.payload.request.AgendaRequest;
import com.example.demo.payload.request.BloqueoRequest;
import com.example.demo.service.AgendaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("api/agendas")
public class AgendaController {

    private final AgendaService agendaService;

    public AgendaController(AgendaService agendaService) {
        this.agendaService = agendaService;
    }

    @GetMapping("/")
    public ResponseEntity<List<AgendaResponseDTO>> searchAgendas(
            @RequestParam(required = false) Long servicio,
            @RequestParam(required = false) Long grupo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate hasta
    ) {
        LocalDateTime desdeDateTime = (desde != null) ? desde.atStartOfDay() : null;
        LocalDateTime hastaDateTime = (hasta != null) ? hasta.atTime(23, 59, 59) : null;

        return ResponseEntity.ok(agendaService.search(servicio, grupo, desdeDateTime, hastaDateTime));
    }


    // Obtener una agenda por ID
    @GetMapping("/{id}")
    public ResponseEntity<AgendaResponseDTO> getById(@PathVariable Long id) {
        AgendaResponseDTO agendaResponseDTO = agendaService.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Agenda no encontrada"));
        return ResponseEntity.ok(agendaResponseDTO);
    }
    // Obtener agendas por grupo
    @GetMapping("/grupo/{grupoId}")
    public List<AgendaResponseDTO> getByGrupo(@PathVariable Long grupoId) {
        System.out.println("Controller: Obteniendo agendas para el grupo ID: " + grupoId);
        return agendaService.getAgendasByGrupo(grupoId);
    }
    // Obtener agendas por servicio
    @GetMapping("/servicio/{servicioId}")
    public List<AgendaResponseDTO> getByServicio(@PathVariable Long servicioId) {
        return agendaService.getAgendasByServicio(servicioId);
    }
    // Obtener agendas por grupo y servicio
    @GetMapping("/grupo/{grupoId}/servicio/{servicioId}")
    public List<AgendaResponseDTO> getByGrupoAndServicio(@PathVariable Long grupoId, @PathVariable Long servicioId) {
        return agendaService.getAgendasByGrupoAndServicio(grupoId, servicioId);
    }

    //Obetener dias disponibles para un servicio
    @GetMapping("/dias/")
    public ResponseEntity<List<LocalDate>> getDiasDisponibles(
            @RequestParam (required = false)Long servicio,
            @RequestParam (required = false) Long grupo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate hasta
    ){
        System.out.println("Controller: Obteniendo dias disponibles para el servicio ID: " + servicio + " y grupo ID: " + grupo);
        LocalDateTime desdeDateTime = (desde != null) ? desde.atStartOfDay() : null;
        LocalDateTime hastaDateTime = (hasta != null) ? hasta.atTime(23, 59, 59) : null;

        return ResponseEntity.ok(agendaService.getDiasDisponiblesParaServicio(servicio, grupo, desdeDateTime, hastaDateTime));
    }

    @PostMapping("/bloquear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> bloquearAgendas(@RequestBody BloqueoRequest request) {
        int cantidad = agendaService.bloquearAgendas(request);
        return ResponseEntity.ok("Se han bloqueado " + cantidad + " agendas correctamente.");
    }
    // Metodos POST
    // Crear una nueva agenda
    @PostMapping("/")
    public ResponseEntity<Agenda> create(@RequestBody AgendaRequest request) {
        Agenda nuevaAgenda = agendaService.create(request);
        return ResponseEntity.ok(nuevaAgenda);
    }

    // Metodos DELETE
    // Borrar una agenda por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        agendaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
