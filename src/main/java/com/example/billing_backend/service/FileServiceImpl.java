package com.example.billing_backend.service;

import com.example.billing_backend.model.FileMetadata;
import com.example.billing_backend.repository.FileMetadataRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileMetadataRepository fileRepository;
    private final String UPLOAD_DIR = "uploads";
    private final Path fileStorageLocation = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
    private final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/jpg",
            "application/pdf",
            "text/csv", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create upload directory!", ex);
        }
    }

    @Override
    @Transactional
    public FileMetadata uploadFile(MultipartFile file, String category, String uploadedBy) {
        // 1. Validation: Empty Check
        if (file.isEmpty()) {
            throw new RuntimeException("Cannot upload an empty file!");
        }

        // 2. Validation: Type Check
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new RuntimeException("Invalid file type! Allowed types are JPG, PNG, PDF, CSV, XLSX.");
        }

        // 3. Validation: Size Check (Max 10MB = 10 * 1024 * 1024 bytes)
        if (file.getSize() > 10485760) {
            throw new RuntimeException("File is too large! Maximum allowed size is 10MB.");
        }

        try {
            // 4. Generate Unique Name
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + extension;

            // 5. Store File physically
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 6. Save Metadata in DB
            FileMetadata metadata = FileMetadata.builder()
                    .fileName(uniqueFileName)
                    .originalName(originalFilename)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .filePath(targetLocation.toString())
                    .category(category.toUpperCase())
                    .uploadedBy(uploadedBy)
                    .build();

            return fileRepository.save(metadata);

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + fileName);
            }
        } catch (Exception ex) {
            throw new RuntimeException("File not found: " + fileName, ex);
        }
    }

    @Override
    public FileMetadata getFileDetails(String fileName) {
        return fileRepository.findByFileName(fileName)
                .orElseThrow(() -> new RuntimeException("File metadata not found!"));
    }

    @Override
    @Transactional
    public void deleteFile(Long fileId) {
        FileMetadata metadata = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found!"));

        try {
            // Delete physically
            Path filePath = this.fileStorageLocation.resolve(metadata.getFileName()).normalize();
            Files.deleteIfExists(filePath);

            // Delete from DB
            fileRepository.delete(metadata);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete physical file!", ex);
        }
    }
}