package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "sports")
public class Sport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false)
    private String name;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "sport_code", nullable = false, unique = true)
    private String sportCode;
    
    @Size(max = 10)
    @Column(name = "icon")
    private String icon;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // Constructors
    public Sport() {}
    
    public Sport(String name, String sportCode, String icon) {
        this.name = name;
        this.sportCode = sportCode;
        this.icon = icon;
        this.isActive = true;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSportCode() {
        return sportCode;
    }
    
    public void setSportCode(String sportCode) {
        this.sportCode = sportCode;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}