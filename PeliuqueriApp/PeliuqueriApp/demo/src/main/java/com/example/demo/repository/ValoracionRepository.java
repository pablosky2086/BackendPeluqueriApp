package com.example.demo.repository;

import com.example.demo.model.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {
    // Para comprobar si una cita ya tiene valoración
    Optional<Valoracion> findByCitaId(Long citaId);

    // 2. Get by Cliente (Devuelve lista)
    // Navegamos: Valoracion -> Cita -> Cliente -> Id
    List<Valoracion> findByCitaClienteId(Long clienteId);

    // 3. Get by Grupo
    // Navegamos: Valoracion -> Cita -> Agenda -> Grupo -> Id
    List<Valoracion> findByCitaAgendaGrupoId(Long grupoId);

    // 4. Get by Servicio
    // Navegamos: Valoracion -> Cita -> Agenda -> Servicio -> Id
    List<Valoracion> findByCitaAgendaServicioId(Long servicioId);

    // 5. Get Between Tiempo (Basado en la fecha de la CITA)
    List<Valoracion> findByCitaFechaHoraInicioBetween(LocalDateTime inicio, LocalDateTime fin);
}