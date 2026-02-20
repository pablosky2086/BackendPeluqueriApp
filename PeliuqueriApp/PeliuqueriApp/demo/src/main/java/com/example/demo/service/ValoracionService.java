package com.example.demo.service;

import com.example.demo.model.Cita;
import com.example.demo.model.Valoracion;
import com.example.demo.payload.request.ValoracionRequest;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.ValoracionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ValoracionService {

    private final ValoracionRepository valoracionRepository;
    private final CitaRepository citaRepository;
    private final AuthService authService;
    private final StorageService storageService;

    public ValoracionService(ValoracionRepository valoracionRepository,
                             CitaRepository citaRepository,
                             AuthService authService,
                             StorageService storageService) {
        this.valoracionRepository = valoracionRepository;
        this.citaRepository = citaRepository;
        this.authService = authService;
        this.storageService = storageService;
    }

    // --- CREATE ---
    public Valoracion crearValoracion(ValoracionRequest request, MultipartFile fichero) {
        // 1. Buscar la cita
        Cita cita = citaRepository.findById(request.getCitaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

        // 2. Seguridad: Permitir si es el Dueño O es Administrador
        boolean esDueño = authService.isOwnerOfCliente(cita.getCliente().getId());
        boolean esAdmin = authService.isAdmin();

        if (!esDueño && !esAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para valorar esta cita");
        }

        // --- NUEVAS VALIDACIONES ---

        // A) Validar que la cita ya haya ocurrido (Fecha actual > Fecha inicio cita)
        if (LocalDateTime.now().isBefore(cita.getFechaHoraInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes valorar una cita futura. Espera a que suceda.");
        }

        // B) Validar que el Admin/Grupo haya confirmado la asistencia
        // IMPORTANTE: Esto requiere que hayas añadido el campo 'confirmada' en tu modelo Cita.java
        if (!cita.isConfirmada()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El centro aún no ha confirmado tu asistencia. Por favor, espera a que la validen para poder valorar.");
        }
        // ---------------------------

        // 3. Regla de negocio: Verificar si ya existe valoración
        if (valoracionRepository.findByCitaId(cita.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta cita ya tiene una valoración");
        }

        // 4. Crear entidad con los 5 campos
        Valoracion valoracion = new Valoracion();
        valoracion.setCita(cita);

        // Asignamos los 5 valores
        valoracion.setTratoPersonal(request.getTratoPersonal());
        valoracion.setDesarrolloServicio(request.getDesarrolloServicio());
        valoracion.setClaridadComunicacion(request.getClaridadComunicacion());
        valoracion.setLimpieza(request.getLimpieza());
        valoracion.setGeneral(request.getGeneral());

        valoracion.setComentario(request.getComentario());

        // 5. Guardar foto si existe
        if (fichero != null && !fichero.isEmpty()) {
            String nombreFichero = storageService.store(fichero);
            valoracion.setFotoUrl(nombreFichero);
        }

        return valoracionRepository.save(valoracion);
    }

    // --- UPDATE ---
    public Valoracion update(Long id, ValoracionRequest request, MultipartFile nuevoFichero) {
        Valoracion valoracion = valoracionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Valoración no encontrada"));

        // Seguridad: Dueño o Admin
        boolean esDueño = authService.isOwnerOfCliente(valoracion.getCita().getCliente().getId());
        boolean esAdmin = authService.isAdmin();

        if (!esDueño && !esAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para editar esta valoración");
        }

        // Actualizar datos solo si vienen en el request
        // Actualizar datos (comprobamos uno a uno si vienen en el request)
        if (request.getTratoPersonal() != null) valoracion.setTratoPersonal(request.getTratoPersonal());
        if (request.getDesarrolloServicio() != null) valoracion.setDesarrolloServicio(request.getDesarrolloServicio());
        if (request.getClaridadComunicacion() != null) valoracion.setClaridadComunicacion(request.getClaridadComunicacion());
        if (request.getLimpieza() != null) valoracion.setLimpieza(request.getLimpieza());
        if (request.getGeneral() != null) valoracion.setGeneral(request.getGeneral());

        if (request.getComentario() != null) valoracion.setComentario(request.getComentario());

        // Gestión de fichero en edición
        if (nuevoFichero != null && !nuevoFichero.isEmpty()) {
            // 1. Borrar foto antigua del disco si tenía una
            if (valoracion.getFotoUrl() != null) {
                storageService.delete(valoracion.getFotoUrl());
            }
            // 2. Guardar foto nueva
            String nombreNuevo = storageService.store(nuevoFichero);
            valoracion.setFotoUrl(nombreNuevo);
        }

        return valoracionRepository.save(valoracion);
    }

    // --- DELETE ---
    public void delete(Long id) {
        Valoracion valoracion = valoracionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Valoración no encontrada"));

        // Seguridad: Dueño o Admin
        boolean esDueño = authService.isOwnerOfCliente(valoracion.getCita().getCliente().getId());
        boolean esAdmin = authService.isAdmin();

        if (!esDueño && !esAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para borrar esta valoración");
        }

        // 1. Limpiar disco (borrar la foto física)
        if (valoracion.getFotoUrl() != null) {
            storageService.delete(valoracion.getFotoUrl());
        }

        // 2. Borrar de BD
        valoracionRepository.delete(valoracion);
    }

    // --- GETTERS (CONSULTAS) ---

    // --- GET ALL ---
    public List<Valoracion> findAll() {
        return valoracionRepository.findAll();
    }

    public Valoracion getByCita(Long citaId) {
        return valoracionRepository.findByCitaId(citaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Valoración no encontrada para esa cita"));
    }

    public List<Valoracion> getByCliente(Long clienteId) {
        return valoracionRepository.findByCitaClienteId(clienteId);
    }

    public List<Valoracion> getByGrupo(Long grupoId) {
        return valoracionRepository.findByCitaAgendaGrupoId(grupoId);
    }

    public List<Valoracion> getByServicio(Long servicioId) {
        return valoracionRepository.findByCitaAgendaServicioId(servicioId);
    }

    public List<Valoracion> getBetweenFechas(LocalDateTime desde, LocalDateTime hasta) {
        return valoracionRepository.findByCitaFechaHoraInicioBetween(desde, hasta);
    }
}