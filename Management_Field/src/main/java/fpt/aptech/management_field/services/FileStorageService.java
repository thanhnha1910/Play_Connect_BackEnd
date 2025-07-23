package fpt.aptech.management_field.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Stores the uploaded file and returns the generated filename
     * @param file The multipart file to store
     * @return The generated filename
     */
    String storeFile(MultipartFile file);

    /**
     * Loads a file as a Resource for download/serving
     * @param fileName The name of the file to load
     * @return Resource representing the file
     */
    Resource loadFileAsResource(String fileName);

    /**
     * Deletes a file from storage
     * @param fileName The name of the file to delete
     * @return true if file was deleted, false otherwise
     */
    boolean deleteFile(String fileName);

    /**
     * Gets the upload directory path
     * @return The absolute path to upload directory
     */
    String getUploadDirectory();
}