package fpt.aptech.management_field.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Entity
@Table(name = "locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long locationId;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String name;
    
    @Column(unique = true, length = 255)
    private String slug;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String address;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;
    
    private String city;
    private String country;
    
    @Column(precision = 8, scale = 6)
    private BigDecimal latitude;
    
    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private Owner owner;
    
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    
    @Lob
    @Column(name = "image_gallery")
    private String imageGallery; // JSON string of URLs
    
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FieldType> fieldTypes = new ArrayList<>();
    
    // Method để tự động tạo slug từ name
    @PrePersist
    @PreUpdate
    public void generateSlug() {
        if (this.name != null && (this.slug == null || this.slug.isEmpty())) {
            this.slug = createSlugFromName(this.name);
        }
    }
    
    // Explicit getters for compatibility
    public String getAddress() {
        return address;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public BigDecimal getLatitude() {
        return latitude;
    }
    
    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }
    
    public BigDecimal getLongitude() {
        return longitude;
    }
    
    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }
    
    public Long getLocationId() {
        return locationId;
    }
    
    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public Owner getOwner() {
        return owner;
    }
    
    public void setOwner(Owner owner) {
        this.owner = owner;
    }
    
    public void setAddress(String address) {
        this.address = address;
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
    
    private String createSlugFromName(String name) {
        if (name == null) {
            return "";
        }
        
        final Pattern NONLATIN = Pattern.compile("[^\\w-]");
        final Pattern WHITESPACE = Pattern.compile("[\\s]");
        
        // Normalize to NFD form to separate accents from letters
        String nowhitespace = WHITESPACE.matcher(name).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        
        // Remove accents (diacritical marks)
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        
        // Convert to lowercase and replace multiple dashes with a single one
        return slug.toLowerCase(Locale.ENGLISH).replaceAll("-{2,}", "-").replaceAll("^-|-$", "");
    }
}