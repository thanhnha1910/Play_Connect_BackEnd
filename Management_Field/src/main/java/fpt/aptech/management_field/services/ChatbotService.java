package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.Field;
import fpt.aptech.management_field.models.Location;
import fpt.aptech.management_field.payload.dtos.AiRequestDTO;
import fpt.aptech.management_field.payload.dtos.AiResponseDTO;
import fpt.aptech.management_field.repositories.FieldRepository;
import fpt.aptech.management_field.repositories.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Value("${ai.service.url:http://localhost:5001}")
    private String aiServiceUrl;

    public String getResponse(String userMessage) {
        try {
            // Gọi AI Service để nhận diện intent
            AiRequestDTO request = new AiRequestDTO();
            request.setMessage(userMessage);
            String url = aiServiceUrl + "/predict";
            
            AiResponseDTO aiResponse = restTemplate.postForObject(url, request, AiResponseDTO.class);
            
            if (aiResponse == null) {
                return "Xin lỗi, tôi không thể xử lý yêu cầu của bạn lúc này. Vui lòng thử lại sau.";
            }

            // Xử lý logic nghiệp vụ dựa trên intent
            return processIntent(aiResponse, userMessage);
            
        } catch (Exception e) {
            return "Xin lỗi, có lỗi xảy ra khi xử lý yêu cầu của bạn. Vui lòng thử lại sau.";
        }
    }

    private String processIntent(AiResponseDTO aiResponse, String userMessage) {
        String intent = aiResponse.getIntent();
        
        switch (intent) {
            case "price_inquiry":
                return handlePriceInquiry(userMessage);
            
            case "location_inquiry":
                return handleLocationInquiry();
            
            case "operating_hours":
                return handleOperatingHours();
            
            case "availability_inquiry":
                return handleAvailabilityInquiry();
            
            case "amenities_inquiry":
                return handleAmenitiesInquiry();
            
            case "booking_management":
                return handleBookingManagement();
            
            case "payment_methods":
                return handlePaymentMethods();
            
            case "fallback":
                return aiResponse.getResponse();
            
            default:
                // Các intent tĩnh như greeting, thanks, goodbye
                return aiResponse.getResponse();
        }
    }

    private String handlePriceInquiry(String userMessage) {
        try {
            // Phân tích từ khóa trong tin nhắn
            String fieldType = extractFieldType(userMessage);
            
            if (fieldType != null) {
                List<Field> fields = fieldRepository.findAll().stream()
                .filter(field -> field.getType() != null && 
                        field.getType().getName().toLowerCase().contains(fieldType.toLowerCase()))
                .collect(Collectors.toList());
                
                if (!fields.isEmpty()) {
                    Field field = fields.get(0);
                    return String.format("Dạ, sân %s có giá là %,.0f VNĐ/giờ ạ. Bạn có muốn xem thêm thông tin về sân này không?", 
                        fieldType, field.getHourlyRate());
                } else {
                    return "Hiện tại chúng tôi chưa có thông tin giá cho loại sân này. Bạn có thể liên hệ trực tiếp để được tư vấn chi tiết.";
                }
            } else {
                // Trả về bảng giá tổng quát
                List<Field> allFields = fieldRepository.findAll();
                if (!allFields.isEmpty()) {
                    StringBuilder priceList = new StringBuilder("Bảng giá các sân của chúng tôi:\n");
                    allFields.stream()
                        .limit(5) // Giới hạn 5 sân đầu tiên
                        .forEach(field -> priceList.append(String.format("- %s: %,.0f VNĐ/giờ\n", 
                            field.getType().getName(), field.getHourlyRate())));
                    priceList.append("Bạn có muốn xem chi tiết về sân nào không?");
                    return priceList.toString();
                }
            }
        } catch (Exception e) {
            // Log error here
        }
        
        return "Xin lỗi, tôi không thể lấy thông tin giá lúc này. Vui lòng liên hệ trực tiếp để được tư vấn.";
    }

    private String handleLocationInquiry() {
        try {
            List<Location> locations = locationRepository.findAll();
            
            if (!locations.isEmpty()) {
                StringBuilder locationList = new StringBuilder("Danh sách địa chỉ các sân của chúng tôi:\n");
                locations.stream()
                   // Giới hạn 5 địa điểm đầu tiên
                    .forEach(location -> locationList.append(String.format("📍 %s - %s\n", 
                        location.getName(), location.getAddress())));
                locationList.append("Bạn muốn xem chi tiết về địa điểm nào?");
                return locationList.toString();
            }
        } catch (Exception e) {
            // Log error here
        }
        
        return "Xin lỗi, tôi không thể lấy thông tin địa chỉ lúc này. Vui lòng liên hệ trực tiếp để được hỗ trợ.";
    }

    private String handleOperatingHours() {
        return "🕐 Giờ hoạt động của chúng tôi:\n" +
               "- Thứ 2 - Thứ 6: 6:00 - 22:00\n" +
               "- Thứ 7 - Chủ nhật: 5:30 - 23:00\n" +
               "- Lễ Tết: 7:00 - 21:00\n\n" +
               "Bạn có muốn đặt sân trong khung giờ nào không?";
    }

    private String handleAvailabilityInquiry() {
        return "Để kiểm tra lịch trống, bạn vui lòng:\n" +
               "1. Truy cập trang web của chúng tôi\n" +
               "2. Chọn sân và ngày muốn đặt\n" +
               "3. Hệ thống sẽ hiển thị các khung giờ còn trống\n\n" +
               "Hoặc bạn có thể cho tôi biết cụ thể ngày và giờ muốn đặt để tôi hỗ trợ kiểm tra.";
    }

    private String handleAmenitiesInquiry() {
        return "🏟️ Tiện ích tại sân của chúng tôi:\n" +
               "✅ Cho thuê bóng và dụng cụ thể thao\n" +
               "✅ Bãi đỗ xe miễn phí\n" +
               "✅ Nước uống và đồ ăn nhẹ\n" +
               "✅ WiFi miễn phí\n" +
               "✅ Phòng thay đồ và tắm rửa\n" +
               "✅ Cho thuê giày thể thao\n\n" +
               "Bạn có cần thêm thông tin về tiện ích nào không?";
    }

    private String handleBookingManagement() {
        return "📋 Quản lý đặt sân:\n" +
               "• Hủy sân: Có thể hủy trước 2 giờ, hoàn 80% tiền\n" +
               "• Đổi giờ: Liên hệ trước 1 giờ để được hỗ trợ\n" +
               "• Xem lịch sử: Đăng nhập tài khoản > Lịch sử đặt sân\n\n" +
               "Bạn cần hỗ trợ thao tác nào cụ thể?";
    }

    private String handlePaymentMethods() {
        return "💳 Các hình thức thanh toán:\n" +
               "✅ Tiền mặt tại sân\n" +
               "✅ Chuyển khoản ngân hàng\n" +
               "✅ Ví điện tử (MoMo, ZaloPay)\n" +
               "✅ Thẻ tín dụng/ghi nợ\n" +
               "✅ PayPal\n\n" +
               "Bạn muốn thanh toán bằng hình thức nào?";
    }

    private String extractFieldType(String message) {
        String lowerMessage = message.toLowerCase();
        
        // Patterns để nhận diện loại sân
        if (lowerMessage.contains("sân 5") || lowerMessage.contains("5 người")) {
            return "Sân 5 người";
        }
        if (lowerMessage.contains("sân 7") || lowerMessage.contains("7 người")) {
            return "Sân 7 người";
        }
        if (lowerMessage.contains("sân 11") || lowerMessage.contains("11 người")) {
            return "Sân 11 người";
        }
        if (lowerMessage.contains("cầu lông") || lowerMessage.contains("badminton")) {
            return "Cầu lông";
        }
        if (lowerMessage.contains("tennis")) {
            return "Tennis";
        }
        if (lowerMessage.contains("bóng rổ") || lowerMessage.contains("basketball")) {
            return "Bóng rổ";
        }
        
        return null;
    }
}