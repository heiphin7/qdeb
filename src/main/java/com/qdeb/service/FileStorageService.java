package com.qdeb.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    public String storeFile(MultipartFile file) {
        try {
            // Создаем директорию если не существует
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Генерируем уникальное имя файла
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String fileName = UUID.randomUUID().toString() + fileExtension;
            Path targetLocation = uploadPath.resolve(fileName);
            
            // Копируем файл
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("Файл сохранен: {}", targetLocation.toString());
            return fileName;
            
        } catch (IOException ex) {
            log.error("Ошибка при сохранении файла: {}", ex.getMessage());
            throw new RuntimeException("Не удалось сохранить файл", ex);
        }
    }
    
    public boolean deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            log.error("Ошибка при удалении файла: {}", ex.getMessage());
            return false;
        }
    }
    
    public Path getFilePath(String fileName) {
        return Paths.get(uploadDir).resolve(fileName);
    }
}
