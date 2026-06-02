package com.example.billing_backend.service;

import com.example.billing_backend.model.FileMetadata;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileMetadata uploadFile(MultipartFile file, String category, String uploadedBy);
    Resource loadFileAsResource(String fileName);
    FileMetadata getFileDetails(String fileName);
    void deleteFile(Long fileId);
}