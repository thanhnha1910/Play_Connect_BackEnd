package fpt.aptech.management_field.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ImageStorageService {
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${app.base.url:http://localhost:1444}")
    private String baseUrl;
    
    public enum ImageType {
        FACILITY("Facility"),
        FIELD("Field"),
        POST_COMMUNITY("post-community");
        
        private final String folderName;
        
        ImageType(String folderName) {
            this.folderName = folderName;
        }
        
        public String getFolderName() {
            return folderName;
        }
    }
    
    public List<String> uploadImages(MultipartFile[] files, ImageType imageType) throws IOException {
        List<String> urls = new ArrayList<>();
        
        // Create upload directory structure: uploads/images/{type}
        Path uploadPath = Paths.get(uploadDir, "images", imageType.getFolderName());
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String originalFilename = file.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                
                // Add timestamp prefix to filename for uniqueness
                String filename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + fileExtension;
                Path filePath = uploadPath.resolve(filename);
                
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                
                String fileUrl = baseUrl + "/uploads/images/" + imageType.getFolderName() + "/" + filename;
                urls.add(fileUrl);
            }
        }
        
        return urls;
    }
    
    public String uploadSingleImage(MultipartFile file, ImageType imageType) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        List<String> urls = uploadImages(new MultipartFile[]{file}, imageType);
        return urls.isEmpty() ? null : urls.get(0);
    }
}