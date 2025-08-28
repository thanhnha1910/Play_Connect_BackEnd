package fpt.aptech.management_field.exception;

public class TournamentRegistrationException extends RuntimeException {
    public TournamentRegistrationException(String message) {
        super(message);
    }
    
    public TournamentRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}