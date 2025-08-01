package fpt.aptech.management_field.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "field_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id")
    private Long typeId;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String name;
    
    @Column(name = "team_capacity")
    private Integer teamCapacity;
    
    @Column(name = "max_capacity")
    private Integer maxCapacity;
    
    @Column(name = "hourly_rate")
    private Integer hourlyRate;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    @JsonIgnore
    private Location location;

    @OneToMany(mappedBy = "type")
    @JsonIgnore
    private List<Field> fields;
    
    // Explicit getters and setters for compatibility
    public Long getTypeId() {
        return typeId;
    }
    
    public void setTypeId(Long typeId) {
        this.typeId = typeId;
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
    
    public Integer getTeamCapacity() {
        return teamCapacity;
    }
    
    public void setTeamCapacity(Integer teamCapacity) {
        this.teamCapacity = teamCapacity;
    }
    
    public Integer getMaxCapacity() {
        return maxCapacity;
    }
    
    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
}