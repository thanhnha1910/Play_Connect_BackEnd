package fpt.aptech.management_field.payload.dtos;

/**
 * DTO for action buttons in chatbot responses
 * Used to provide interactive buttons for users
 */
public class ActionButtonDTO {
    private String action;
    private String label;
    private String value;
    
    public ActionButtonDTO() {}
    
    public ActionButtonDTO(String action, String label, String value) {
        this.action = action;
        this.label = label;
        this.value = value;
    }
    
    // Getters and setters
    public String getAction() { 
        return action; 
    }
    
    public void setAction(String action) { 
        this.action = action; 
    }
    
    public String getLabel() { 
        return label; 
    }
    
    public void setLabel(String label) { 
        this.label = label; 
    }
    
    public String getValue() { 
        return value; 
    }
    
    public void setValue(String value) { 
        this.value = value; 
    }
    
    @Override
    public String toString() {
        return "ActionButtonDTO{" +
                "action='" + action + '\'' +
                ", label='" + label + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}