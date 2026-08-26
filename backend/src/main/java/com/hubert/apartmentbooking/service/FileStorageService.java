package com.hubert.apartmentbooking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    public String storeApartmentPhoto(Long apartmentId, MultipartFile file) throws IOException {
        String extension = extractExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;

        Path targetDir = Path.of(uploadDir, "apartments", apartmentId.toString());
        Files.createDirectories(targetDir);

        Path targetFile = targetDir.resolve(fileName);
        file.transferTo(targetFile);

        return fileName;
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }

    public void deleteApartmentPhoto(Long apartmentId, String fileName) throws IOException {
        Path filePath = Path.of(uploadDir, "apartments", apartmentId.toString(), fileName);
        Files.deleteIfExists(filePath);
    }
}