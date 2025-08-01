package fpt.aptech.management_field.payload.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadResponse {
    private String fileName;
    private String fileUrl;
    private String message;
    private List<String> urls;
    
    public FileUploadResponse(List<String> urls) {
        this.urls = urls;
        this.message = "Files uploaded successfully";
    }
}