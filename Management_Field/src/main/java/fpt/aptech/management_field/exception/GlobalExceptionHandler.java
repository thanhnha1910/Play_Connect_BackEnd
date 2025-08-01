package fpt.aptech.management_field.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiBaseException.class)
    @ResponseBody
    public ErrorResponse handleApiExceptions(ApiBaseException ex) {
        return new ErrorResponse(ex.getMessage());
    }
}
