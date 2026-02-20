package com.example.demo.controller;

import com.example.demo.model.Cita;
import com.example.demo.payload.request.NewCitaRequest;
import com.example.demo.service.AuthService;
import com.example.demo.service.CitaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("api/citas")
public class CitaController {

    private final CitaService citaService;
    private final AuthService authService;

    public CitaController(CitaService citaService, AuthService authService) {
        this.citaService = citaService;
        this.authService = authService;
    }

    // Metodos GET
    // Obtener todas las citas
    @GetMapping("/")
    @PreAuthorize("hasAnyRole('ADMIN', 'GRUPO')")
    public List<Cita> getAll() {
        return citaService.findAll();
    }

    // Obtener una cita por ID
    @GetMapping("/{id}")
    public ResponseEntity<Cita> getById(@PathVariable Long id) {
        Cita cita = citaService.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));
        if (!authService.isAdmin() && !authService.isGrupo() && cita.getCliente().getId() != authService.getCurrentUserId()){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para acceder a este recurso");
        } else {
            return new ResponseEntity<Cita>(cita, HttpStatus.OK);
        }
    }

    // Obtener citas por cliente
    @GetMapping("/cliente/{clienteId}")
    public List<Cita> getByCliente(@PathVariable Long clienteId) {
        if (!authService.isAdmin() && !authService.isGrupo() && !authService.isOwnerOfCliente(clienteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para acceder a este recurso");
        }
        return citaService.getCitasByCliente(clienteId);
    }

    // Obtener citas por grupo
    @GetMapping("/grupo/{grupoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GRUPO')")
    public List<Cita> getByGrupo(@PathVariable Long grupoId) {
        return citaService.getCitasByGrupo(grupoId);
    }

    // Obtener citas por servicio
    @GetMapping("/servicio/{servicioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GRUPO')")
    public List<Cita> getByServicio(@PathVariable Long servicioId) {
        return citaService.getCitasByServicio(servicioId);
    }

    // Obtener citas entre fechas
    @GetMapping("/fechas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GRUPO')")
    public List<Cita> getCitasEntreFechas(
            @RequestParam("desde") String desde,
            @RequestParam("hasta") String hasta) {
        return citaService.getCitasEntreFechas(
                java.time.LocalDateTime.parse(desde),
                java.time.LocalDateTime.parse(hasta));
    }

    // Obtener citas de un grupo entre fechas
    @GetMapping("/grupo/{grupoId}/fechas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GRUPO')")
    public List<Cita> getCitasGrupoEntreFechas(
            @PathVariable Long grupoId,
            @RequestParam("desde") String desde,
            @RequestParam("hasta") String hasta) {
        return citaService.getCitasGrupoEntreFechas(
                grupoId,
                java.time.LocalDateTime.parse(desde),
                java.time.LocalDateTime.parse(hasta));
    }

    // Obtener citas por agenda
    @GetMapping("/agenda/{agendaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GRUPO')")
    public List<Cita> getByAgenda(@PathVariable Long agendaId) {
        return citaService.getCitasByAgenda(agendaId);
    }

    // POST - Crear nueva cita
    @PostMapping("/")
    public ResponseEntity<Cita> create(@RequestBody NewCitaRequest request) {
        Cita nuevaCita = citaService.create(request);
        return new ResponseEntity<>(nuevaCita, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GRUPO')") // Asumo que el Grupo también puede confirmar que atendió al cliente
    public ResponseEntity<Cita> confirmarAsistencia(@PathVariable Long id) {
        Cita citaConfirmada = citaService.marcarComoConfirmada(id);
        return ResponseEntity.ok(citaConfirmada);
    }

    // DELETE - Cancelar una cita
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Cita cita = citaService.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));
        if (!authService.isAdmin() && !authService.isGrupo() && cita.getCliente().getId() != authService.getCurrentUserId()){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para cancelar esta cita");
        }
        citaService.cancelarPorId(id);
        return ResponseEntity.noContent().build();
    }
}
