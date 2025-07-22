package com.chelly.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    @Value("${upload.dir}")
    private String uploadDir;

    @Value("${server.port:8080}")
    private String serverPort;

    public String storeFile(MultipartFile file) {
        try {
            // Buat folder upload jika belum ada
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Bersihkan nama file
            String originalFilename = file.getOriginalFilename();
            String cleanFilename = originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
            String fileName = System.currentTimeMillis() + "_" + cleanFilename;

            // Simpan file
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Kembalikan URL file yang bisa diakses dari browser
            return "/uploads/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Gagal menyimpan file", e);
        }
    }
}
