package fpt.aptech.management_field.services;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SystemMessageService {
    
    private final Map<String, String> systemMessages;
    
    public SystemMessageService() {
        this.systemMessages = new HashMap<>();
        initializeSystemMessages();
    }
    
    private void initializeSystemMessages() {
        // Greeting messages
        systemMessages.put("greeting", "Xin chào! Tôi là trợ lý ảo của hệ thống đặt sân. Tôi có thể giúp bạn:\n" +
                "• Kiểm tra giá sân\n" +
                "• Tìm địa điểm sân\n" +
                "• Xem lịch trống\n" +
                "• Đặt sân\n" +
                "• Quản lý booking\n\n" +
                "Bạn cần hỗ trợ gì ạ?");
        
        systemMessages.put("goodbye", "Cảm ơn bạn đã sử dụng dịch vụ! Chúc bạn có những trận đấu vui vẻ! 👋");
        
        systemMessages.put("thanks", "Không có gì ạ! Tôi luôn sẵn sàng hỗ trợ bạn. Bạn còn cần hỗ trợ gì khác không?");
        
        // Error messages
        systemMessages.put("error.general", "Xin lỗi, có lỗi xảy ra. Vui lòng thử lại sau.");
        
        systemMessages.put("error.ai_service", "Xin lỗi, tôi không thể xử lý yêu cầu của bạn lúc này. Vui lòng thử lại sau.");
        
        // Clarification questions
        systemMessages.put("clarify.field_type", "Bạn muốn tìm hiểu về loại sân nào? (Sân 5 người, 7 người, 11 người, cầu lông, tennis, bóng rổ)");
        
        systemMessages.put("clarify.location", "Bạn muốn tìm sân ở khu vực nào?");
        
        systemMessages.put("clarify.time", "Bạn muốn đặt sân vào thời gian nào? (Ví dụ: tối mai, 19h ngày 15/8)");
        
        systemMessages.put("clarify.date", "Bạn muốn đặt sân vào ngày nào?");
        
        // Insufficient information messages
        systemMessages.put("insufficient.booking_info", "Để đặt sân, tôi cần thêm thông tin:\n" +
                "• Loại sân\n" +
                "• Thời gian\n" +
                "• Địa điểm (nếu có)\n\n" +
                "Bạn có thể cung cấp thêm thông tin này không?");
        
        systemMessages.put("insufficient.availability_info", "Để kiểm tra lịch trống, tôi cần:\n" +
                "• Loại sân\n" +
                "• Ngày muốn đặt\n\n" +
                "Bạn có thể cho tôi biết thêm không?");
        
        // Fallback message
        systemMessages.put("fallback", "Xin lỗi, tôi chưa hiểu rõ yêu cầu của bạn. Bạn có thể nói rõ hơn được không?\n\n" +
                "Tôi có thể hỗ trợ bạn:\n" +
                "• Xem giá sân\n" +
                "• Tìm địa điểm\n" +
                "• Kiểm tra lịch trống\n" +
                "• Đặt sân\n" +
                "• Quản lý booking");
    }
    
    public String getMessage(String key) {
        return systemMessages.getOrDefault(key, systemMessages.get("fallback"));
    }
    
    public String getMessage(String key, Object... params) {
        String message = getMessage(key);
        return String.format(message, params);
    }
    
    public void updateMessage(String key, String message) {
        systemMessages.put(key, message);
    }
}