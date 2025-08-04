package fpt.aptech.management_field.exception;

import fpt.aptech.management_field.payload.response.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = getVietnameseErrorMessage(fieldName, error.getDefaultMessage());
            errors.put(fieldName, errorMessage);
        });
        
        // Tạo thông báo lỗi tổng hợp
        StringBuilder errorMessage = new StringBuilder("Đăng ký thất bại: ");
        errors.values().forEach(msg -> errorMessage.append(msg).append(". "));
        
        return ResponseEntity.badRequest()
                .body(new MessageResponse(errorMessage.toString().trim()));
    }
    
    private String getVietnameseErrorMessage(String fieldName, String defaultMessage) {
        switch (fieldName) {
            case "username":
                if (defaultMessage.contains("size must be between")) {
                    return "Tên đăng nhập phải có từ 3 đến 20 ký tự";
                } else if (defaultMessage.contains("must not be blank")) {
                    return "Tên đăng nhập không được để trống";
                }
                break;
            case "email":
                if (defaultMessage.contains("Email should be valid")) {
                    return "Email không đúng định dạng";
                } else if (defaultMessage.contains("must not be blank")) {
                    return "Email không được để trống";
                } else if (defaultMessage.contains("size must be")) {
                    return "Email không được vượt quá 50 ký tự";
                }
                break;
            case "password":
                if (defaultMessage.contains("size must be between")) {
                    return "Mật khẩu phải có từ 6 đến 40 ký tự";
                } else if (defaultMessage.contains("must not be blank")) {
                    return "Mật khẩu không được để trống";
                }
                break;
            case "fullName":
                if (defaultMessage.contains("must not be blank")) {
                    return "Họ và tên không được để trống";
                }
                break;
            case "sportType":
                if (defaultMessage.contains("Sport type is required")) {
                    return "Vui lòng chọn môn thể thao";
                }
                break;
            case "locationDescription":
                if (defaultMessage.contains("Location description is required")) {
                    return "Vui lòng nhập khu vực";
                } else if (defaultMessage.contains("must not exceed 500 characters")) {
                    return "Mô tả khu vực không được vượt quá 500 ký tự";
                }
                break;
            case "estimatedStartTime":
                if (defaultMessage.contains("Estimated start time is required")) {
                    return "Vui lòng chọn thời gian bắt đầu";
                } else if (defaultMessage.contains("must be in the future")) {
                    return "Thời gian bắt đầu phải trong tương lai";
                }
                break;
            case "estimatedEndTime":
                if (defaultMessage.contains("Estimated end time is required")) {
                    return "Vui lòng chọn thời gian kết thúc";
                } else if (defaultMessage.contains("must be in the future")) {
                    return "Thời gian kết thúc phải trong tương lai";
                } else if (defaultMessage.contains("End time must be after start time")) {
                    return "Thời gian kết thúc phải sau thời gian bắt đầu";
                }
                break;
            case "slotsNeeded":
                if (defaultMessage.contains("Slots needed is required")) {
                    return "Vui lòng nhập số người cần tìm";
                } else if (defaultMessage.contains("must be at least 1")) {
                    return "Số người cần tìm phải lớn hơn 0";
                } else if (defaultMessage.contains("must not exceed 50")) {
                    return "Số người cần tìm không được vượt quá 50";
                }
                break;
            case "skillLevel":
                if (defaultMessage.contains("Skill level is required")) {
                    return "Vui lòng chọn trình độ";
                } else if (defaultMessage.contains("must be one of")) {
                    return "Trình độ không hợp lệ";
                }
                break;
            default:
                return defaultMessage;
        }
        return defaultMessage;
    }
}