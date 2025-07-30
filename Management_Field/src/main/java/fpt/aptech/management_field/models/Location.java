package fpt.aptech.management_field.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
    
    private String name;
    
    @Column(unique = true, length = 255)
    private String slug;
    
    private String address;
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
    
    // Method để tự động tạo slug từ name
    @PrePersist
    @PreUpdate
    public void generateSlug() {
        if (this.name != null && (this.slug == null || this.slug.isEmpty())) {
            this.slug = createSlugFromName(this.name);
        }
    }
    
    private String createSlugFromName(String name) {
        return name.toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}