package fpt.aptech.management_field.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;
    private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    // Allowed file types for security
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public FileStorageServiceImpl(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(System.getProperty("user.dir"))
                .resolve(uploadDir)
                .normalize()
                .toAbsolutePath();

        try {
            Files.createDirectories(this.fileStorageLocation);
            logger.info("File storage directory created/verified at: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            logger.error("Could not create upload directory: {}", this.fileStorageLocation, ex);
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        // Validate file
        validateFile(file);

        // Generate unique filename with timestamp
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + "." + fileExtension;

        try {
            // Check for invalid characters in filename
            if (originalFilename.contains("..")) {
                throw new RuntimeException("Filename contains invalid path sequence: " + originalFilename);
            }

            Path targetLocation = this.fileStorageLocation.resolve(fileName);

            // Use REPLACE_EXISTING to handle any conflicts
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            logger.info("File stored successfully: {} -> {}", originalFilename, fileName);
            return fileName;

        } catch (IOException ex) {
            logger.error("Failed to store file: {}", originalFilename, ex);
            throw new RuntimeException("Could not store file " + originalFilename + ". Please try again!", ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + fileName);
            }
        } catch (Exception ex) {
            throw new RuntimeException("File not found: " + fileName, ex);
        }
    }

    @Override
    public boolean deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            logger.error("Failed to delete file: {}", fileName, ex);
            return false;
        }
    }

    @Override
    public String getUploadDirectory() {
        return this.fileStorageLocation.toString();
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size exceeds maximum allowed size of " + MAX_FILE_SIZE + " bytes");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new RuntimeException("File must have a valid name");
        }

        String fileExtension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            throw new RuntimeException("File type not allowed. Allowed types: " + ALLOWED_EXTENSIONS);
        }

        // Validate MIME type as additional security
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("File must be an image");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
