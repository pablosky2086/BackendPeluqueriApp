package com.example.demo.controller;

import com.example.demo.model.Valoracion;
import com.example.demo.payload.request.ValoracionRequest;
import com.example.demo.service.ValoracionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("api/valoraciones")
public class ValoracionController {

    private final ValoracionService valoracionService;

    public ValoracionController(ValoracionService valoracionService) {
        this.valoracionService = valoracionService;
    }

    // --- CREATE ---
    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Valoracion> create(
            @RequestPart("datos") ValoracionRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        Valoracion nuevaValoracion = valoracionService.crearValoracion(request, file);
        return ResponseEntity.ok(nuevaValoracion);
    }

    // --- UPDATE ---
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Valoracion> update(
            @PathVariable Long id,
            @RequestPart("datos") ValoracionRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        Valoracion valoracionActualizada = valoracionService.update(id, request, file);
        return ResponseEntity.ok(valoracionActualizada);
    }

    // --- DELETE ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        valoracionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- GETTERS ---

    // --- GET ALL ---
    @GetMapping("/")
    public ResponseEntity<List<Valoracion>> getAll() {
        return ResponseEntity.ok(valoracionService.findAll());
    }

    @GetMapping("/cita/{citaId}")
    public ResponseEntity<Valoracion> getByCita(@PathVariable Long citaId) {
        return ResponseEntity.ok(valoracionService.getByCita(citaId));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Valoracion>> getByCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(valoracionService.getByCliente(clienteId));
    }

    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<List<Valoracion>> getByGrupo(@PathVariable Long grupoId) {
        return ResponseEntity.ok(valoracionService.getByGrupo(grupoId));
    }

    @GetMapping("/servicio/{servicioId}")
    public ResponseEntity<List<Valoracion>> getByServicio(@PathVariable Long servicioId) {
        return ResponseEntity.ok(valoracionService.getByServicio(servicioId));
    }

    @GetMapping("/fechas")
    public ResponseEntity<List<Valoracion>> getBetweenFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta
    ) {
        return ResponseEntity.ok(valoracionService.getBetweenFechas(desde, hasta));
    }
}