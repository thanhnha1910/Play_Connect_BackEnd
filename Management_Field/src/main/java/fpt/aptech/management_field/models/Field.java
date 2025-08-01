package fpt.aptech.management_field.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "fields")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Field {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "field_id")
    private Long fieldId;

    @Column(name = "name", columnDefinition = "NVARCHAR(MAX)")
    private String name;

    @Column(name = "hourly_rate")
    private Integer hourlyRate;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private FieldCategory category;
    
    @ManyToOne
    @JoinColumn(name = "type_id")
    private FieldType type;
    
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    
    @Lob
    @Column(name = "image_gallery")
    private String imageGallery; // JSON string of URLs
    
    @PrePersist
    public void prePersist() {
        if (isActive == null) {
            isActive = true;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    // Explicit getter for isActive to ensure proper access
    public Boolean isActive() {
        return isActive;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public Integer getHourlyRate() {
        return hourlyRate;
    }
    
    public void setHourlyRate(Integer hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
    
    public Long getFieldId() {
        return fieldId;
    }
    
    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }
    
    public FieldType getType() {
        return type;
    }
    
    public void setType(FieldType type) {
        this.type = type;
    }
    
    public FieldCategory getCategory() {
        return category;
    }
    
    public void setCategory(FieldCategory category) {
        this.category = category;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<FieldClosure> getFieldClosures() {
        return fieldClosures;
    }
    
    public void setFieldClosures(List<FieldClosure> fieldClosures) {
        this.fieldClosures = fieldClosures;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    
    public String getImageGallery() {
        return imageGallery;
    }
    
    public void setImageGallery(String imageGallery) {
        this.imageGallery = imageGallery;
    }

    @OneToMany(mappedBy = "field", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<FieldClosure> fieldClosures;
}