package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageService {

    @Value("${storage.location}")
    private String storageLocation;

    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Fichero vacío");
        }
        try {
            Path root = Paths.get(storageLocation);
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            // Generar nombre único para evitar colisiones
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path destinationFile = root.resolve(Paths.get(filename))
                    .normalize().toAbsolutePath();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return filename; // Retornamos solo el nombre del archivo
        } catch (IOException e) {
            throw new RuntimeException("Error al almacenar el fichero", e);
        }
    }

    public void delete(String filename) {
        if (filename == null || filename.isEmpty()) return;
        try {
            Path file = Paths.get(storageLocation).resolve(filename);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Error al borrar el fichero: " + filename, e);
        }
    }
}