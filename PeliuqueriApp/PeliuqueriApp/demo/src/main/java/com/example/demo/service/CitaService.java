package com.example.demo.service;

import com.example.demo.model.Agenda;
import com.example.demo.model.Cita;
import com.example.demo.model.Cliente;
import com.example.demo.model.Servicio;
import com.example.demo.payload.request.NewCitaRequest;
import com.example.demo.repository.AgendaRepository;
import com.example.demo.repository.CitaRepository;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final AgendaRepository agendaRepository;
    private final ClienteService clienteService;
    private final AuthService authService;

    public CitaService(CitaRepository citaRepository,
                       AgendaRepository agendaRepository,
                       ClienteService clienteService, AuthService authService) {
        this.citaRepository = citaRepository;
        this.agendaRepository = agendaRepository;
        this.clienteService = clienteService;
        this.authService = authService;
    }

    // ---------------- GET ----------------

    public List<Cita> findAll() {
        return citaRepository.findAll();
    }

    public Optional<Cita> findById(Long id) {
        return citaRepository.findById(id);
    }

    public List<Cita> getCitasByCliente(Long clienteId) {
        return citaRepository.findByClienteId(clienteId);
    }

    public List<Cita> getCitasByGrupo(Long grupoId) {
        return citaRepository.findByAgendaGrupoId(grupoId);
    }

    public List<Cita> getCitasByServicio(Long servicioId) {
        return citaRepository.findByAgendaServicioId(servicioId);
    }

    public List<Cita> getCitasEntreFechas(LocalDateTime desde, LocalDateTime hasta) {
        return citaRepository.findByAgendaHoraInicioBetween(desde, hasta);
    }

    public List<Cita> getCitasGrupoEntreFechas(
            Long grupoId,
            LocalDateTime desde,
            LocalDateTime hasta) {

        return citaRepository
                .findByAgendaGrupoIdAndAgendaHoraInicioBetween(grupoId, desde, hasta);
    }

    public List<Cita> getCitasByAgenda(Long agendaId) {
        return citaRepository.findByAgendaId(agendaId);
    }

    // ---------------- POST ----------------

    public Cita create(NewCitaRequest request) {

        // Buscar agenda
        Agenda agenda = agendaRepository.findById(request.getAgendaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Agenda no encontrada"));

        // Buscar cliente
        Cliente cliente = clienteService.findById(request.getClienteId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        // Autorización (YA con datos reales)
        if (!authService.isAdmin()
                && !authService.isGrupo()
                && !cliente.getId().equals(authService.getCurrentUserId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para crear una cita para este cliente"
            );
        }

        // Regla de negocio: El cliente sólo puede tener cita en los huecos de la agenda
        List<LocalDateTime> huecos = agenda.calcularHuecos();
        LocalDateTime hora = LocalDateTime.parse(request.getFechaHoraInicio());
        if (!huecos.contains(hora)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La hora de la cita no coincide con los huecos disponibles de la agenda"
            );
        }

        if (!agenda.esDisponible(hora)){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A esa hora no quedan sillas disponibles"
            );
        }


//        if (inicio.isBefore(agenda.getHoraInicio())) {
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "La hora de inicio de la cita no puede ser antes que la hora de inicio de la agenda"
//            );
//        }

        // Hora de fin
//        Servicio servicio = agenda.getServicio();
//        LocalDateTime finCita = inicio.plusMinutes(servicio.getDuracion());
//
//        if (finCita.isAfter(agenda.getHoraFin())) {
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "La hora de fin de la cita excede la hora de fin de la agenda"
//            );
//        }

        //  Crear y guardar
        Cita cita = new Cita(hora, agenda, cliente);
        return citaRepository.save(cita);
    }

    public Cita marcarComoConfirmada(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

        cita.setConfirmada(true);
        return citaRepository.save(cita);
    }

    // ---------------- DELETE ----------------

    public void cancelarPorId(Long id) {
        if (!citaRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Cita no encontrada"
            );
        }
        citaRepository.deleteById(id);
    }
}
