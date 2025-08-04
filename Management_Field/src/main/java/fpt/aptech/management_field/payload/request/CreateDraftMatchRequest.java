package fpt.aptech.management_field.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CreateDraftMatchRequest.EndTimeAfterStartTimeValidator.class)
@interface EndTimeAfterStartTime {
    String message() default "End time must be after start time";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@EndTimeAfterStartTime
public class CreateDraftMatchRequest {
    @NotBlank(message = "Sport type is required")
    private String sportType;
    
    @NotBlank(message = "Location description is required")
    @Size(max = 500, message = "Location description must not exceed 500 characters")
    private String locationDescription;
    
    @NotNull(message = "Estimated start time is required")
    @Future(message = "Estimated start time must be in the future")
    private LocalDateTime estimatedStartTime;
    
    @NotNull(message = "Estimated end time is required")
    @Future(message = "Estimated end time must be in the future")
    private LocalDateTime estimatedEndTime;
    
    @NotNull(message = "Slots needed is required")
    @Min(value = 1, message = "Slots needed must be at least 1")
    @Max(value = 50, message = "Slots needed must not exceed 50")
    private Integer slotsNeeded;
    
    @NotBlank(message = "Skill level is required")
    @Pattern(regexp = "^(BEGINNER|INTERMEDIATE|ADVANCED|EXPERT|ANY)$", 
             message = "Skill level must be one of: BEGINNER, INTERMEDIATE, ADVANCED, EXPERT, ANY")
    private String skillLevel;
    
    private List<String> requiredTags;
    
    // Explicit getters for compatibility
    public String getSportType() {
        return sportType;
    }
    
    public String getLocationDescription() {
        return locationDescription;
    }
    
    public LocalDateTime getEstimatedStartTime() {
        return estimatedStartTime;
    }
    
    public LocalDateTime getEstimatedEndTime() {
        return estimatedEndTime;
    }
    
    public Integer getSlotsNeeded() {
        return slotsNeeded;
    }
    
    public String getSkillLevel() {
        return skillLevel;
    }
    
    public List<String> getRequiredTags() {
        return requiredTags;
    }
    
    // Custom validator for end time after start time
    public static class EndTimeAfterStartTimeValidator implements ConstraintValidator<EndTimeAfterStartTime, CreateDraftMatchRequest> {
        @Override
        public void initialize(EndTimeAfterStartTime constraintAnnotation) {
        }
        
        @Override
        public boolean isValid(CreateDraftMatchRequest request, ConstraintValidatorContext context) {
            if (request.getEstimatedStartTime() == null || request.getEstimatedEndTime() == null) {
                return true; // Let @NotNull handle null validation
            }
            return request.getEstimatedEndTime().isAfter(request.getEstimatedStartTime());
        }
    }
}