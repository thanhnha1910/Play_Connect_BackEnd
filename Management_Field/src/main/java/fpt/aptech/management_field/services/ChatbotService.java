package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.ConversationContext;
import fpt.aptech.management_field.models.Field;
import fpt.aptech.management_field.models.Location;
import fpt.aptech.management_field.payload.dtos.*;
import fpt.aptech.management_field.repositories.FieldRepository;
import fpt.aptech.management_field.repositories.LocationRepository;
import fpt.aptech.management_field.repositories.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;

@Service
public class ChatbotService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private LocationRepository locationRepository;
    
    @Autowired
    private ConversationContextService contextService;
    
    @Autowired
    private SystemMessageService messageService;
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private FieldService fieldService;
    
    @Autowired
    private TournamentRepository tournamentRepository;

    @Value("${ai.service.url:http://localhost:5002}")
    private String aiServiceUrl;

    public ChatbotResponseDTO processMessage(ChatbotRequestDTO request) {
        try {
            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = contextService.generateSessionId();
            }
            
            // Lấy context hiện tại
            ConversationContext context = contextService.getContext(sessionId);
            
            // Gọi AI Service để nhận diện intent và entities
            AiRequestV2DTO aiRequest = new AiRequestV2DTO();
            aiRequest.setMessage(request.getMessage());
            aiRequest.setContext(context.getEntities());
            
            String url = aiServiceUrl + "/predict";
            AiResponseV2DTO aiResponse = restTemplate.postForObject(url, aiRequest, AiResponseV2DTO.class);
            
            if (aiResponse == null) {
                return createErrorResponse(sessionId, messageService.getMessage("error.ai_service"));
            }
            
            // Cập nhật context với intent và entities mới
            context = contextService.updateContext(sessionId, aiResponse.getIntent(), 
                    aiResponse.getEntities(), request.getMessage());
            
            // Xử lý logic nghiệp vụ dựa trên intent và context
            return processIntentWithContext(aiResponse, context);
            
        } catch (Exception e) {
            String sessionId = request.getSessionId() != null ? request.getSessionId() : contextService.generateSessionId();
            return createErrorResponse(sessionId, messageService.getMessage("error.general"));
        }
    }

    private ChatbotResponseDTO processIntentWithContext(AiResponseV2DTO aiResponse, ConversationContext context) {
        String intent = aiResponse.getIntent();
        String sessionId = context.getSessionId();
        
        switch (intent) {
            case "greeting":
                return createSimpleResponse(sessionId, messageService.getMessage("greeting"), context.getEntities());
            case "goodbye":
                contextService.clearContext(sessionId);
                return createSimpleResponse(sessionId, messageService.getMessage("goodbye"), new HashMap<>());
            case "thanks":
                return createSimpleResponse(sessionId, messageService.getMessage("thanks"), context.getEntities());
            case "price_inquiry":
                return handlePriceInquiry(context);
            case "location_inquiry":
                return handleLocationInquiry(context);
            case "operating_hours":
                return handleOperatingHours(context);
            case "availability_inquiry":
                return handleAvailabilityInquiry(context);
            case "amenities_inquiry":
                return handleAmenitiesInquiry(context);
            case "booking_management":
                return handleBookingManagement(context);
            case "payment_methods":
                return handlePaymentMethods(context);
            case "booking_instruction":
                return handleBookingInstruction(context);
            case "tournament_inquiry":
                return handleTournamentInquiry(context);
            case "tournament_schedule":
                return handleTournamentSchedule(context);
            case "tournament_registration":
                return handleTournamentRegistration(context);
            case "tournament_fee_inquiry":
                return handleTournamentFeeInquiry(context);
            case "tournament_prize":
                return handleTournamentPrize(context);
            default:
                return createSimpleResponse(sessionId, messageService.getMessage("fallback"), context.getEntities());
        }
    }
    
    private ChatbotResponseDTO createErrorResponse(String sessionId, String message) {
        ChatbotResponseDTO response = new ChatbotResponseDTO();
        response.setText(message);
        response.setSessionId(sessionId);
        response.setActions(new ArrayList<>());
        response.setContext(new HashMap<>());
        return response;
    }
    
    private ChatbotResponseDTO createSimpleResponse(String sessionId, String text, Map<String, Object> entities) {
        ChatbotResponseDTO response = new ChatbotResponseDTO();
        response.setText(text);
        response.setSessionId(sessionId);
        response.setActions(new ArrayList<>());
        response.setContext(entities);
        return response;
    }

    private ChatbotResponseDTO handlePriceInquiry(ConversationContext context) {
        String sessionId = context.getSessionId();
        Map<String, Object> entities = context.getEntities();
        
        try {
            String fieldType = (String) entities.get("field_type");
            String location = (String) entities.get("location");
            
            // Case 1: Có cả field_type và location
            if (fieldType != null && location != null) {
                return handleSpecificPriceInquiry(sessionId, fieldType, location, entities);
            }
            
            // Case 2: Chỉ có field_type
            if (fieldType != null) {
                return handleFieldTypePriceInquiry(sessionId, fieldType, entities);
            }
            
            // Case 3: Chỉ có location
            if (location != null) {
                return handleLocationPriceInquiry(sessionId, location, entities);
            }
            
            // Case 4: Không có thông tin cụ thể - trả về bảng giá tổng quát
            return handleGeneralPriceInquiry(sessionId, entities);
            
        } catch (Exception e) {
            System.err.println("Error in handlePriceInquiry: " + e.getMessage());
            return createSimpleResponse(sessionId, 
                "Xin lỗi, tôi không thể lấy thông tin giá lúc này. Vui lòng liên hệ trực tiếp để được tư vấn.", 
                entities);
        }
    }

    private ChatbotResponseDTO handleSpecificPriceInquiry(String sessionId, String fieldType, String location, Map<String, Object> entities) {
        List<Field> fields = findFields(fieldType, location);
        
        if (fields.isEmpty()) {
            // Không tìm thấy sân phù hợp
            return createClarificationResponse(sessionId, 
                String.format("Hiện tại chúng tôi chưa có sân %s tại %s. Bạn có muốn xem các sân khác không?", fieldType, location),
                getSuggestedAlternatives(fieldType, location),
                "field_suggestion",
                entities);
        }
        
        if (fields.size() == 1) {
            // Tìm thấy đúng 1 sân
            Field field = fields.get(0);
            String message = String.format(
                "Dạ, sân %s tại %s có giá %,.0f VNĐ/giờ.\n" +
                "📍 Địa chỉ: %s\n" +
                "⚽ Sức chứa: %d người\n" +
                "Bạn có muốn đặt sân này không?",
                field.getType().getName(),
                field.getLocation().getName(),
                field.getHourlyRate(),
                field.getLocation().getAddress(),
                field.getType().getMaxCapacity()
            );
            
            // Thêm action buttons
            ChatbotResponseDTO response = createSimpleResponse(sessionId, message, entities);
            List<ChatbotResponseDTO.ActionDTO> actions = new ArrayList<>();
            
            ChatbotResponseDTO.ActionDTO bookAction = new ChatbotResponseDTO.ActionDTO();
            bookAction.setLabel("Đặt sân ngay");
            bookAction.setType("action");
            Map<String, Object> bookPayload = new HashMap<>();
            bookPayload.put("action", "book_field");
            bookPayload.put("fieldId", field.getFieldId().toString());
            bookAction.setPayload(bookPayload);
            actions.add(bookAction);
            
            ChatbotResponseDTO.ActionDTO detailAction = new ChatbotResponseDTO.ActionDTO();
            detailAction.setLabel("Xem chi tiết");
            detailAction.setType("action");
            Map<String, Object> detailPayload = new HashMap<>();
            detailPayload.put("action", "view_details");
            detailPayload.put("fieldId", field.getFieldId().toString());
            detailAction.setPayload(detailPayload);
            actions.add(detailAction);
            
            response.setActions(actions);
            return response;
        }
        
        // Tìm thấy nhiều sân - hiển thị danh sách
        return createMultipleFieldsResponse(sessionId, fields, fieldType, location, entities);
    }

    private ChatbotResponseDTO handleFieldTypePriceInquiry(String sessionId, String fieldType, Map<String, Object> entities) {
        List<Field> fields = findFields(fieldType, null);
        
        if (fields.isEmpty()) {
            return createSimpleResponse(sessionId, 
                String.format("Hiện tại chúng tôi chưa có sân %s. Bạn có muốn xem các loại sân khác không?", fieldType), 
                entities);
        }
        
        // Group by location để hiển thị giá theo từng địa điểm
        Map<String, List<Field>> fieldsByLocation = fields.stream()
            .collect(Collectors.groupingBy(field -> field.getLocation().getName()));
        
        StringBuilder message = new StringBuilder();
        message.append(String.format("Giá sân %s tại các địa điểm:\n\n", fieldType));
        
        fieldsByLocation.forEach((locationName, locationFields) -> {
            message.append(String.format("📍 %s:\n", locationName));
            locationFields.forEach(field -> {
                message.append(String.format("   • %s: %,.0f VNĐ/giờ\n", 
                    field.getType().getName(), field.getHourlyRate()));
            });
            message.append("\n");
        });
        
        message.append("Bạn muốn xem chi tiết sân nào?");
        
        return createSimpleResponse(sessionId, message.toString(), entities);
    }

    private ChatbotResponseDTO handleLocationPriceInquiry(String sessionId, String location, Map<String, Object> entities) {
        List<Field> fields = findFields(null, location);
        
        if (fields.isEmpty()) {
            return createSimpleResponse(sessionId, 
                String.format("Hiện tại chúng tôi chưa có sân tại %s. Bạn có muốn xem các địa điểm khác không?", location), 
                entities);
        }
        
        // Group by field type
        Map<String, List<Field>> fieldsByType = fields.stream()
            .collect(Collectors.groupingBy(field -> field.getType().getName()));
        
        StringBuilder message = new StringBuilder();
        message.append(String.format("Bảng giá các sân tại %s:\n\n", location));
        
        fieldsByType.forEach((typeName, typeFields) -> {
            double avgPrice = typeFields.stream()
                .mapToDouble(Field::getHourlyRate)
                .average()
                .orElse(0);
            
            message.append(String.format("⚽ %s: %,.0f VNĐ/giờ\n", typeName, avgPrice));
        });
        
        message.append("\nBạn muốn đặt loại sân nào?");
        
        return createSimpleResponse(sessionId, message.toString(), entities);
    }

    private ChatbotResponseDTO handleGeneralPriceInquiry(String sessionId, Map<String, Object> entities) {
        List<Field> allFields = fieldRepository.findAll();
        
        if (allFields.isEmpty()) {
            return createSimpleResponse(sessionId, 
                "Hiện tại hệ thống chưa có thông tin sân. Vui lòng liên hệ trực tiếp.", 
                entities);
        }
        
        // Group by field type và tính giá trung bình
        Map<String, Double> avgPricesByType = allFields.stream()
            .collect(Collectors.groupingBy(
                field -> field.getType().getName(),
                Collectors.averagingDouble(Field::getHourlyRate)
            ));
        
        StringBuilder message = new StringBuilder();
        message.append("📋 Bảng giá sân của chúng tôi:\n\n");
        
        avgPricesByType.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .forEach(entry -> {
                message.append(String.format("⚽ %s: từ %,.0f VNĐ/giờ\n", 
                    entry.getKey(), entry.getValue()));
            });
        
        message.append("\n💡 Giá có thể khác nhau tùy theo địa điểm và thời gian.\n");
        message.append("Bạn muốn xem giá cụ thể cho loại sân nào?");
        
        return createSimpleResponse(sessionId, message.toString(), entities);
    }

    private List<String> getSuggestedAlternatives(String fieldType, String location) {
        List<String> suggestions = new ArrayList<>();
        
        // Tìm field types tương tự
        List<Field> similarFields = fieldRepository.findAll().stream()
            .filter(field -> field.getType().getName().toLowerCase().contains(fieldType.toLowerCase().substring(0, Math.min(3, fieldType.length()))))
            .limit(3)
            .collect(Collectors.toList());
        
        similarFields.forEach(field -> {
            suggestions.add(String.format("%s tại %s", 
                field.getType().getName(), field.getLocation().getName()));
        });
        
        // Tìm locations tương tự
        List<Field> nearbyFields = fieldRepository.findAll().stream()
            .filter(field -> field.getLocation().getName().toLowerCase().contains(location.toLowerCase().substring(0, Math.min(3, location.length()))))
            .limit(2)
            .collect(Collectors.toList());
        
        nearbyFields.forEach(field -> {
            suggestions.add(String.format("%s tại %s", 
                field.getType().getName(), field.getLocation().getName()));
        });
        
        return suggestions.stream().distinct().limit(5).collect(Collectors.toList());
    }

    private ChatbotResponseDTO createMultipleFieldsResponse(String sessionId, List<Field> fields, 
            String fieldType, String location, Map<String, Object> entities) {
        
        StringBuilder message = new StringBuilder();
        message.append(String.format("Tìm thấy %d sân %s tại %s:\n\n", 
            fields.size(), fieldType, location));
        
        fields.stream().limit(5).forEach(field -> {
            message.append(String.format("🏟️ %s\n", field.getType().getName()));
            message.append(String.format("   💰 Giá: %,.0f VNĐ/giờ\n", field.getHourlyRate()));
            message.append(String.format("   📍 %s\n", field.getLocation().getAddress()));
            message.append(String.format("   👥 Sức chứa: %d người\n\n", field.getType().getMaxCapacity()));
        });
        
        if (fields.size() > 5) {
            message.append(String.format("... và %d sân khác\n\n", fields.size() - 5));
        }
        
        message.append("Bạn muốn đặt sân nào?");
        
        ChatbotResponseDTO response = createSimpleResponse(sessionId, message.toString(), entities);
        
        // Thêm action buttons cho từng sân
        List<ChatbotResponseDTO.ActionDTO> actions = new ArrayList<>();
        fields.stream().limit(3).forEach(field -> {
            ChatbotResponseDTO.ActionDTO action = new ChatbotResponseDTO.ActionDTO();
            action.setLabel(String.format("Đặt %s", field.getType().getName()));
            action.setType("action");
            Map<String, Object> payload = new HashMap<>();
            payload.put("action", "book_field");
            payload.put("fieldId", field.getFieldId().toString());
            action.setPayload(payload);
            actions.add(action);
        });
        
        ChatbotResponseDTO.ActionDTO viewAllAction = new ChatbotResponseDTO.ActionDTO();
        viewAllAction.setLabel("Xem tất cả");
        viewAllAction.setType("action");
        Map<String, Object> viewAllPayload = new HashMap<>();
        viewAllPayload.put("action", "view_all");
        viewAllAction.setPayload(viewAllPayload);
        actions.add(viewAllAction);
        
        response.setActions(actions);
        return response;
    }

    private ChatbotResponseDTO handleLocationInquiry(ConversationContext context) {
        String sessionId = context.getSessionId();
        Map<String, Object> entities = context.getEntities();
        
        try {
            List<Location> locations = locationRepository.findAll();
            
            if (!locations.isEmpty()) {
                StringBuilder locationList = new StringBuilder("Danh sách địa chỉ các sân của chúng tôi:\n");
                locations.stream()
                   // Giới hạn 5 địa điểm đầu tiên
                    .forEach(location -> locationList.append(String.format("📍 %s - %s\n", 
                        location.getName(), location.getAddress())));
                locationList.append("Bạn muốn xem chi tiết về địa điểm nào?");
                return createSimpleResponse(sessionId, locationList.toString(), entities);
            }
        } catch (Exception e) {
            // Log error here
        }
        
        return createSimpleResponse(sessionId, 
            "Xin lỗi, tôi không thể lấy thông tin địa chỉ lúc này. Vui lòng liên hệ trực tiếp để được hỗ trợ.", 
            entities);
    }

    private ChatbotResponseDTO handleOperatingHours(ConversationContext context) {
        String sessionId = context.getSessionId();
        Map<String, Object> entities = context.getEntities();
        
        String message = "🕐 Giờ hoạt động của chúng tôi:\n" +
               "- Thứ 2 - Thứ 6: 6:00 - 22:00\n" +
               "- Thứ 7 - Chủ nhật: 5:30 - 23:00\n" +
               "- Lễ Tết: 7:00 - 21:00\n\n" +
               "Bạn có muốn đặt sân trong khung giờ nào không?";
        
        return createSimpleResponse(sessionId, message, entities);
    }

    private ChatbotResponseDTO handleAvailabilityInquiry(ConversationContext context) {
        String sessionId = context.getSessionId();
        Map<String, Object> entities = context.getEntities();
        
        String message = "Để kiểm tra lịch trống, bạn vui lòng:\n" +
               "1. Truy cập trang web của chúng tôi\n" +
               "2. Chọn sân và ngày muốn đặt\n" +
               "3. Hệ thống sẽ hiển thị các khung giờ còn trống\n\n" +
               "Hoặc bạn có thể cho tôi biết cụ thể ngày và giờ muốn đặt để tôi hỗ trợ kiểm tra.";
        
        return createSimpleResponse(sessionId, message, entities);
    }

    private ChatbotResponseDTO handleAmenitiesInquiry(ConversationContext context) {
        String sessionId = context.getSessionId();
        Map<String, Object> entities = context.getEntities();
        
        String message = "🏟️ Tiện ích tại sân của chúng tôi:\n" +
               "✅ Cho thuê bóng và dụng cụ thể thao\n" +
               "✅ Bãi đỗ xe miễn phí\n" +
               "✅ Nước uống và đồ ăn nhẹ\n" +
               "✅ WiFi miễn phí\n" +
               "✅ Phòng thay đồ và tắm rửa\n" +
               "✅ Cho thuê giày thể thao\n\n" +
               "Bạn có cần thêm thông tin về tiện ích nào không?";
        
        return createSimpleResponse(sessionId, message, entities);
    }

    private ChatbotResponseDTO handleBookingManagement(ConversationContext context) {
        String sessionId = context.getSessionId();
        Map<String, Object> entities = context.getEntities();
        
        String message = "📋 Quản lý đặt sân:\n" +
               "• Hủy sân: Có thể hủy trước 2 giờ, hoàn 80% tiền\n" +
               "• Đổi giờ: Liên hệ trước 1 giờ để được hỗ trợ\n" +
               "• Xem lịch sử: Đăng nhập tài khoản > Lịch sử đặt sân\n\n" +
               "Bạn cần hỗ trợ thao tác nào cụ thể?";
        
        return createSimpleResponse(sessionId, message, entities);
    }

    private ChatbotResponseDTO handlePaymentMethods(ConversationContext context) {
        String sessionId = context.getSessionId();
        Map<String, Object> entities = context.getEntities();
        
        String message = "💳 Các hình thức thanh toán:\n" +
               "✅ Tiền mặt tại sân\n" +
               "✅ Chuyển khoản ngân hàng\n" +
               "✅ Ví điện tử (MoMo, ZaloPay)\n" +
               "✅ Thẻ tín dụng/ghi nợ\n" +
               "✅ PayPal\n\n" +
               "Bạn muốn thanh toán bằng hình thức nào?";
        
        return createSimpleResponse(sessionId, message, entities);
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
    
    private List<Field> findFields(String fieldType, String location) {
        List<Field> allFields = fieldRepository.findAll();
        
        return allFields.stream()
                .filter(field -> fieldType == null || isFieldTypeMatch(field.getType().getName(), fieldType))
                .filter(field -> location == null || isLocationMatch(field.getLocation().getName(), location))
                .sorted((f1, f2) -> {
                    // Sắp xếp theo độ tương đồng và giá
                    double score1 = calculateFieldScore(f1, fieldType, location);
                    double score2 = calculateFieldScore(f2, fieldType, location);
                    int scoreCompare = Double.compare(score2, score1); // Điểm cao hơn trước
                    if (scoreCompare != 0) return scoreCompare;
                    return Double.compare(f1.getHourlyRate(), f2.getHourlyRate()); // Giá thấp hơn trước
                })
                .collect(Collectors.toList());
    }
    
    private boolean isFieldTypeMatch(String actualType, String searchType) {
        if (searchType == null) return true;
        
        String actual = actualType.toLowerCase().trim();
        String search = searchType.toLowerCase().trim();
        
        // Exact match
        if (actual.equals(search)) return true;
        
        // Contains match
        if (actual.contains(search) || search.contains(actual)) return true;
        
        // Fuzzy matching cho các từ phổ biến
        return calculateSimilarity(actual, search) > 0.6;
    }
    
    private boolean isLocationMatch(String actualLocation, String searchLocation) {
        if (searchLocation == null) return true;
        
        String actual = actualLocation.toLowerCase().trim();
        String search = searchLocation.toLowerCase().trim();
        
        // Exact match
        if (actual.equals(search)) return true;
        
        // Contains match
        if (actual.contains(search) || search.contains(actual)) return true;
        
        // Fuzzy matching
        return calculateSimilarity(actual, search) > 0.7;
    }
    
    private double calculateFieldScore(Field field, String fieldType, String location) {
        double score = 0.0;
        
        // Điểm cho field type match
        if (fieldType != null) {
            score += calculateSimilarity(field.getType().getName().toLowerCase(), fieldType.toLowerCase()) * 0.6;
        }
        
        // Điểm cho location match
        if (location != null) {
            score += calculateSimilarity(field.getLocation().getName().toLowerCase(), location.toLowerCase()) * 0.4;
        }
        
        return score;
    }
    
    private double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        if (s1.equals(s2)) return 1.0;
        
        // Levenshtein distance based similarity
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;
        
        int distance = levenshteinDistance(s1, s2);
        return 1.0 - (double) distance / maxLen;
    }
    
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    private ChatbotResponseDTO createClarificationResponse(String sessionId, String message, 
            List<String> options, String entityType, Map<String, Object> entities) {
        ChatbotResponseDTO response = new ChatbotResponseDTO();
        response.setText(message);
        response.setSessionId(sessionId);
        response.setContext(entities);
        
        List<ChatbotResponseDTO.ActionDTO> actions = new ArrayList<>();
        for (String option : options) {
            ChatbotResponseDTO.ActionDTO action = new ChatbotResponseDTO.ActionDTO();
            action.setLabel(option);
            action.setType("quick_reply");
            Map<String, Object> payload = new HashMap<>();
            payload.put(entityType, option.toLowerCase());
            action.setPayload(payload);
            actions.add(action);
        }
        response.setActions(actions);
        
        return response;
    }
    
    private ChatbotResponseDTO handleBookingInstruction(ConversationContext context) {
        String sessionId = context.getSessionId();
        Map<String, Object> entities = context.getEntities();
        
        String message = "📝 Hướng dẫn đặt sân:\n" +
               "1. Truy cập trang web hoặc ứng dụng\n" +
               "2. Chọn loại sân và địa điểm\n" +
               "3. Chọn ngày và giờ muốn đặt\n" +
               "4. Điền thông tin liên hệ\n" +
               "5. Chọn phương thức thanh toán\n" +
               "6. Xác nhận đặt sân\n\n" +
               "Bạn có cần hỗ trợ thêm về quy trình đặt sân không?";
        
        return createSimpleResponse(sessionId, message, entities);
    }
    
    // ==================== TOURNAMENT INQUIRY METHODS ====================
    
    private ChatbotResponseDTO handleTournamentInquiry(ConversationContext context) {
        String sessionId = context.getSessionId();
        Map<String, Object> entities = context.getEntities();
        
        String tournamentType = (String) entities.get("tournament_type");
        String location = (String) entities.get("location");
        String tournamentName = (String) entities.get("tournament_name");
        
        if (tournamentName != null && !tournamentName.isEmpty()) {
            return handleSpecificTournamentInquiry(sessionId, tournamentName, entities);
        } else if (tournamentType != null && location != null) {
            return handleTournamentByTypeAndLocation(sessionId, tournamentType, location, entities);
        } else if (tournamentType != null) {
            return handleTournamentByType(sessionId, tournamentType, entities);
        } else if (location != null) {
            return handleTournamentByLocation(sessionId, location, entities);
        } else {
            return handleGeneralTournamentInquiry(sessionId, entities);
        }
    }
    
    private ChatbotResponseDTO handleSpecificTournamentInquiry(String sessionId, String tournamentName, Map<String, Object> entities) {
        try {
            List<TournamentDto> tournaments = tournamentRepository.findByNameContainingIgnoreCase(tournamentName);
            
            if (tournaments.isEmpty()) {
                String response = "Xin lỗi, tôi không tìm thấy giải đấu nào có tên \"" + tournamentName + "\".\n" +
                        "Bạn có thể xem danh sách các giải đấu hiện có không?";
                
                List<ChatbotResponseDTO.ActionDTO> actionButtons = Arrays.asList(
                    new ChatbotResponseDTO.ActionDTO("Xem tất cả giải đấu", "action", Map.of("action", "view_all_tournaments")),
                    new ChatbotResponseDTO.ActionDTO("Tìm giải đấu khác", "action", Map.of("action", "search_tournaments"))
                );
                
                return new ChatbotResponseDTO(response, actionButtons, sessionId, entities);
            }
            
            return createTournamentListResponse(sessionId, tournaments, "Thông tin giải đấu \"" + tournamentName + "\":", entities);
            
        } catch (Exception e) {
            return createErrorResponse(sessionId, "Có lỗi xảy ra khi tìm kiếm giải đấu. Vui lòng thử lại.");
        }
    }
    
    private ChatbotResponseDTO handleTournamentByTypeAndLocation(String sessionId, String tournamentType, String location, Map<String, Object> entities) {
        try {
            List<TournamentDto> tournaments = tournamentRepository.findByLocationNameContainingIgnoreCase(location);
            
            // Filter by tournament type if needed (based on description or name)
            tournaments = tournaments.stream()
                .filter(t -> isTournamentTypeMatch(t, tournamentType))
                .collect(Collectors.toList());
            
            if (tournaments.isEmpty()) {
                String response = "Hiện tại không có giải đấu " + tournamentType + " nào tại " + location + ".\n" +
                        "Bạn có muốn xem các giải đấu khác không?";
                
                List<ChatbotResponseDTO.ActionDTO> actionButtons = Arrays.asList(
                    new ChatbotResponseDTO.ActionDTO("Xem giải đấu tại " + location, "action", Map.of("action", "tournaments_by_location_" + location)),
                    new ChatbotResponseDTO.ActionDTO("Xem giải đấu " + tournamentType, "action", Map.of("action", "tournaments_by_type_" + tournamentType)),
                    new ChatbotResponseDTO.ActionDTO("Xem tất cả giải đấu", "action", Map.of("action", "view_all_tournaments"))
                );
                
                return new ChatbotResponseDTO(response, actionButtons, sessionId, entities);
            }
            
            return createTournamentListResponse(sessionId, tournaments, "Giải đấu " + tournamentType + " tại " + location + ":", entities);
            
        } catch (Exception e) {
            return createErrorResponse(sessionId, "Có lỗi xảy ra khi tìm kiếm giải đấu. Vui lòng thử lại.");
        }
    }
    
    private ChatbotResponseDTO handleTournamentByType(String sessionId, String tournamentType, Map<String, Object> entities) {
        try {
            List<TournamentDto> allTournaments = tournamentRepository.findAllOrderByStartDate();
            
            // Filter by tournament type
            List<TournamentDto> tournaments = allTournaments.stream()
                .filter(t -> isTournamentTypeMatch(t, tournamentType))
                .collect(Collectors.toList());
            
            if (tournaments.isEmpty()) {
                String response = "Hiện tại không có giải đấu " + tournamentType + " nào.\n" +
                        "Bạn có muốn xem các loại giải đấu khác không?";
                
                List<ChatbotResponseDTO.ActionDTO> actionButtons = Arrays.asList(
                    new ChatbotResponseDTO.ActionDTO("Xem tất cả giải đấu", "action", Map.of("action", "view_all_tournaments")),
                    new ChatbotResponseDTO.ActionDTO("Tìm theo địa điểm", "action", Map.of("action", "search_by_location"))
                );
                
                return new ChatbotResponseDTO(response, actionButtons, sessionId, entities);
            }
            
            return createTournamentListResponse(sessionId, tournaments, "Các giải đấu " + tournamentType + ":", entities);
            
        } catch (Exception e) {
            return createErrorResponse(sessionId, "Có lỗi xảy ra khi tìm kiếm giải đấu. Vui lòng thử lại.");
        }
    }
    
    private ChatbotResponseDTO handleTournamentByLocation(String sessionId, String location, Map<String, Object> entities) {
        try {
            List<TournamentDto> tournaments = tournamentRepository.findByLocationNameContainingIgnoreCase(location);
            
            if (tournaments.isEmpty()) {
                String response = "Hiện tại không có giải đấu nào tại " + location + ".\n" +
                        "Bạn có muốn xem giải đấu tại các địa điểm khác không?";
                
                List<ChatbotResponseDTO.ActionDTO> actionButtons = Arrays.asList(
                    new ChatbotResponseDTO.ActionDTO("Xem tất cả giải đấu", "action", Map.of("action", "view_all_tournaments")),
                    new ChatbotResponseDTO.ActionDTO("Tìm theo loại giải", "action", Map.of("action", "search_by_type"))
                );
                
                return new ChatbotResponseDTO(response, actionButtons, sessionId, entities);
            }
            
            return createTournamentListResponse(sessionId, tournaments, "Các giải đấu tại " + location + ":", entities);
            
        } catch (Exception e) {
            return createErrorResponse(sessionId, "Có lỗi xảy ra khi tìm kiếm giải đấu. Vui lòng thử lại.");
        }
    }
    
    private ChatbotResponseDTO handleGeneralTournamentInquiry(String sessionId, Map<String, Object> entities) {
        try {
            List<TournamentDto> upcomingTournaments = tournamentRepository.findUpcomingTournaments(java.time.LocalDateTime.now());
            
            if (upcomingTournaments.isEmpty()) {
                String response = "Hiện tại chưa có giải đấu nào sắp diễn ra.\n" +
                        "Bạn có muốn xem tất cả các giải đấu không?";
                
                List<ChatbotResponseDTO.ActionDTO> actionButtons = Arrays.asList(
                    new ChatbotResponseDTO.ActionDTO("Xem tất cả giải đấu", "action", Map.of("action", "view_all_tournaments")),
                    new ChatbotResponseDTO.ActionDTO("Đăng ký nhận thông báo", "action", Map.of("action", "subscribe_notifications"))
                );
                
                return new ChatbotResponseDTO(response, actionButtons, sessionId, entities);
            }
            
            return createTournamentListResponse(sessionId, upcomingTournaments, "Các giải đấu sắp diễn ra:", entities);
            
        } catch (Exception e) {
            return createErrorResponse(sessionId, "Có lỗi xảy ra khi tìm kiếm giải đấu. Vui lòng thử lại.");
        }
    }
    
    private ChatbotResponseDTO handleTournamentSchedule(ConversationContext context) {
        String sessionId = context.getSessionId();
        Map<String, Object> entities = context.getEntities();
        
        String tournamentName = (String) entities.get("tournament_name");
        
        if (tournamentName != null && !tournamentName.isEmpty()) {
            List<TournamentDto> tournaments = tournamentRepository.findByNameContainingIgnoreCase(tournamentName);
            
            if (!tournaments.isEmpty()) {
                TournamentDto tournament = tournaments.get(0);
                String response = createTournamentScheduleText(tournament);
                
                List<ChatbotResponseDTO.ActionDTO> actionButtons = Arrays.asList(
                    new ChatbotResponseDTO.ActionDTO("Đăng ký tham gia", "action", Map.of("action", "register_tournament_" + tournament.getTournamentId())),
                    new ChatbotResponseDTO.ActionDTO("Xem chi tiết", "action", Map.of("action", "view_tournament_details_" + tournament.getTournamentId())),
                    new ChatbotResponseDTO.ActionDTO("Liên hệ BTC", "action", Map.of("action", "contact_organizer"))
                );
                
                return new ChatbotResponseDTO(response, actionButtons, sessionId, entities);
            }
        }
        
        return handleGeneralTournamentInquiry(sessionId, entities);
    }
    
    private ChatbotResponseDTO handleTournamentRegistration(ConversationContext context) {
        String sessionId = context.getSessionId();
        Map<String, Object> entities = context.getEntities();
        
        String response = "Để đăng ký tham gia giải đấu, bạn cần:\n" +
                "1. Chọn giải đấu phù hợp\n" +
                "2. Chuẩn bị đội hình (nếu là giải đấu đồng đội)\n" +
                "3. Nộp phí tham gia\n" +
                "4. Hoàn tất thủ tục đăng ký\n" +
                "\nBạn muốn đăng ký giải đấu nào?";
        
        List<ChatbotResponseDTO.ActionDTO> actionButtons = Arrays.asList(
            new ChatbotResponseDTO.ActionDTO("Xem giải đấu sắp tới", "action", Map.of("action", "view_upcoming_tournaments")),
            new ChatbotResponseDTO.ActionDTO("Hướng dẫn đăng ký", "action", Map.of("action", "registration_guide")),
            new ChatbotResponseDTO.ActionDTO("Liên hệ hỗ trợ", "action", Map.of("action", "contact_support"))
        );
        
        return new ChatbotResponseDTO(response, actionButtons, sessionId, entities);
    }
    
    private ChatbotResponseDTO handleTournamentFeeInquiry(ConversationContext context) {
        String sessionId = context.getSessionId();
        Map<String, Object> entities = context.getEntities();
        
        String tournamentName = (String) entities.get("tournament_name");
        String tournamentType = (String) entities.get("tournament_type");
        
        if (tournamentName != null && !tournamentName.isEmpty()) {
            List<TournamentDto> tournaments = tournamentRepository.findByNameContainingIgnoreCase(tournamentName);
            
            if (!tournaments.isEmpty()) {
                TournamentDto tournament = tournaments.get(0);
                String response = createTournamentFeeText(tournament);
                
                List<ChatbotResponseDTO.ActionDTO> actionButtons = Arrays.asList(
                    new ChatbotResponseDTO.ActionDTO("Đăng ký ngay", "action", Map.of("action", "register_tournament_" + tournament.getTournamentId())),
                    new ChatbotResponseDTO.ActionDTO("Xem chi tiết", "action", Map.of("action", "view_tournament_details_" + tournament.getTournamentId())),
                    new ChatbotResponseDTO.ActionDTO("So sánh giải khác", "action", Map.of("action", "compare_tournaments"))
                );
                
                return new ChatbotResponseDTO(response, actionButtons, sessionId, entities);
            }
        }
        
        // General fee inquiry
        try {
            List<TournamentDto> allTournaments = tournamentRepository.findAllOrderByStartDate();
            
            if (tournamentType != null) {
                allTournaments = allTournaments.stream()
                    .filter(t -> isTournamentTypeMatch(t, tournamentType))
                    .collect(Collectors.toList());
            }
            
            if (allTournaments.isEmpty()) {
                return createErrorResponse(sessionId, "Hiện tại không có thông tin phí tham gia cho loại giải đấu này.");
            }
            
            return createTournamentFeeListResponse(sessionId, allTournaments, entities);
            
        } catch (Exception e) {
            return createErrorResponse(sessionId, "Có lỗi xảy ra khi tìm kiếm thông tin phí tham gia.");
        }
    }
    
    private ChatbotResponseDTO handleTournamentPrize(ConversationContext context) {
        String sessionId = context.getSessionId();
        Map<String, Object> entities = context.getEntities();
        
        String tournamentName = (String) entities.get("tournament_name");
        
        if (tournamentName != null && !tournamentName.isEmpty()) {
            List<TournamentDto> tournaments = tournamentRepository.findByNameContainingIgnoreCase(tournamentName);
            
            if (!tournaments.isEmpty()) {
                TournamentDto tournament = tournaments.get(0);
                String response = createTournamentPrizeText(tournament);
                
                List<ChatbotResponseDTO.ActionDTO> actionButtons = Arrays.asList(
                    new ChatbotResponseDTO.ActionDTO("Đăng ký tham gia", "action", Map.of("action", "register_tournament_" + tournament.getTournamentId())),
                    new ChatbotResponseDTO.ActionDTO("Xem thể thức", "action", Map.of("action", "view_tournament_format_" + tournament.getTournamentId())),
                    new ChatbotResponseDTO.ActionDTO("Xem giải khác", "action", Map.of("action", "view_other_tournaments"))
                );
                
                return new ChatbotResponseDTO(response, actionButtons, sessionId, entities);
            }
        }
        
        // General prize inquiry
        try {
            List<TournamentDto> allTournaments = tournamentRepository.findAllOrderByStartDate();
            
            if (allTournaments.isEmpty()) {
                return createErrorResponse(sessionId, "Hiện tại không có thông tin giải thưởng.");
            }
            
            return createTournamentPrizeListResponse(sessionId, allTournaments, entities);
            
        } catch (Exception e) {
            return createErrorResponse(sessionId, "Có lỗi xảy ra khi tìm kiếm thông tin giải thưởng.");
        }
    }
    
    // ==================== TOURNAMENT HELPER METHODS ====================
    
    private boolean isTournamentTypeMatch(TournamentDto tournament, String searchType) {
        if (searchType == null || searchType.isEmpty()) return true;
        
        String name = tournament.getName().toLowerCase();
        String description = tournament.getDescription() != null ? tournament.getDescription().toLowerCase() : "";
        String search = searchType.toLowerCase();
        
        // Check for common tournament types
        if (search.contains("futsal") || search.contains("5 người")) {
            return name.contains("futsal") || name.contains("5") || description.contains("futsal");
        }
        if (search.contains("bóng đá") || search.contains("7 người") || search.contains("11 người")) {
            return name.contains("bóng đá") || name.contains("football") || name.contains("7") || name.contains("11");
        }
        if (search.contains("tennis")) {
            return name.contains("tennis") || description.contains("tennis");
        }
        if (search.contains("cầu lông") || search.contains("badminton")) {
            return name.contains("cầu lông") || name.contains("badminton") || description.contains("cầu lông");
        }
        if (search.contains("bóng rổ") || search.contains("basketball")) {
            return name.contains("bóng rổ") || name.contains("basketball") || description.contains("bóng rổ");
        }
        
        return name.contains(search) || description.contains(search);
    }
    
    private ChatbotResponseDTO createTournamentListResponse(String sessionId, List<TournamentDto> tournaments, String title, Map<String, Object> entities) {
        StringBuilder response = new StringBuilder(title + "\n\n");
        List<ChatbotResponseDTO.ActionDTO> actionButtons = new ArrayList<>();
        
        int count = Math.min(tournaments.size(), 5); // Limit to 5 tournaments
        
        for (int i = 0; i < count; i++) {
            TournamentDto tournament = tournaments.get(i);
            response.append("🏆 ").append(tournament.getName()).append("\n");
            response.append("📍 ").append(tournament.getLocation().getName()).append("\n");
            response.append("📅 ").append(formatTournamentDate(tournament)).append("\n");
            response.append("💰 Phí tham gia: ").append(formatPrice(tournament.getEntryFee().doubleValue())).append("\n");
            response.append("🎁 Giải thưởng: ").append(formatPrice(tournament.getPrize().doubleValue())).append("\n");
            response.append("👥 Số lượng: ").append(tournament.getSlots()).append(" đội\n\n");
            
            actionButtons.add(new ChatbotResponseDTO.ActionDTO("Đăng ký " + tournament.getName(), "action", Map.of("action", "register_tournament_" + tournament.getTournamentId())));
        }
        
        if (tournaments.size() > 5) {
            response.append("... và ").append(tournaments.size() - 5).append(" giải đấu khác\n");
            actionButtons.add(new ChatbotResponseDTO.ActionDTO("Xem tất cả", "action", Map.of("action", "view_all_tournaments")));
        }
        
        actionButtons.add(new ChatbotResponseDTO.ActionDTO("Liên hệ BTC", "action", Map.of("action", "contact_organizer")));
        
        return new ChatbotResponseDTO(response.toString(), actionButtons, sessionId, entities);
    }
    
    private String createTournamentScheduleText(TournamentDto tournament) {
        return "📅 Lịch thi đấu: " + tournament.getName() + "\n\n" +
                "🏁 Bắt đầu: " + formatDateTime(tournament.getStartDate()) + "\n" +
                "🏁 Kết thúc: " + formatDateTime(tournament.getEndDate()) + "\n" +
                "📍 Địa điểm: " + tournament.getLocation().getName() + "\n" +
                "📍 Địa chỉ: " + tournament.getLocation().getAddress() + "\n" +
                "💰 Phí tham gia: " + formatPrice(tournament.getEntryFee().doubleValue()) + "\n" +
                "🎁 Giải thưởng: " + formatPrice(tournament.getPrize().doubleValue()) + "\n" +
                "👥 Số đội tối đa: " + tournament.getSlots() + "\n" +
                "📊 Trạng thái: " + formatStatus(tournament.getStatus());
    }
    
    private String createTournamentFeeText(TournamentDto tournament) {
        return "💰 Phí tham gia: " + tournament.getName() + "\n\n" +
                "💵 Lệ phí: " + formatPrice(tournament.getEntryFee().doubleValue()) + "\n" +
                "🎁 Giải thưởng: " + formatPrice(tournament.getPrize().doubleValue()) + "\n" +
                "📍 Địa điểm: " + tournament.getLocation().getName() + "\n" +
                "📅 Thời gian: " + formatTournamentDate(tournament) + "\n" +
                "👥 Số đội: " + tournament.getSlots() + "\n\n" +
                "💡 Phí bao gồm: Tham gia thi đấu, giải thưởng, và các tiện ích tại sân.";
    }
    
    private String createTournamentPrizeText(TournamentDto tournament) {
        return "🎁 Giải thưởng: " + tournament.getName() + "\n\n" +
                "🏆 Tổng giải thưởng: " + formatPrice(tournament.getPrize().doubleValue()) + "\n" +
                "💰 Phí tham gia: " + formatPrice(tournament.getEntryFee().doubleValue()) + "\n" +
                "📍 Địa điểm: " + tournament.getLocation().getName() + "\n" +
                "📅 Thời gian: " + formatTournamentDate(tournament) + "\n" +
                "👥 Số đội: " + tournament.getSlots() + "\n\n" +
                "🏅 Cơ cấu giải thưởng sẽ được công bố chi tiết khi giải đấu bắt đầu.";
    }
    
    private ChatbotResponseDTO createTournamentFeeListResponse(String sessionId, List<TournamentDto> tournaments, Map<String, Object> entities) {
        StringBuilder response = new StringBuilder("💰 Bảng phí tham gia các giải đấu:\n\n");
        List<ChatbotResponseDTO.ActionDTO> actionButtons = new ArrayList<>();
        
        int count = Math.min(tournaments.size(), 5);
        
        for (int i = 0; i < count; i++) {
            TournamentDto tournament = tournaments.get(i);
            response.append("🏆 ").append(tournament.getName()).append("\n");
            response.append("💵 Phí: ").append(formatPrice(tournament.getEntryFee().doubleValue())).append("\n");
            response.append("🎁 Thưởng: ").append(formatPrice(tournament.getPrize().doubleValue())).append("\n\n");
            
            actionButtons.add(new ChatbotResponseDTO.ActionDTO("Chi tiết " + tournament.getName(), "action", Map.of("action", "view_tournament_details_" + tournament.getTournamentId())));
        }
        
        actionButtons.add(new ChatbotResponseDTO.ActionDTO("So sánh giải đấu", "action", Map.of("action", "compare_tournaments")));
        
        return new ChatbotResponseDTO(response.toString(), actionButtons, sessionId, entities);
    }
    
    private ChatbotResponseDTO createTournamentPrizeListResponse(String sessionId, List<TournamentDto> tournaments, Map<String, Object> entities) {
        StringBuilder response = new StringBuilder("🎁 Giải thưởng các tournament:\n\n");
        List<ChatbotResponseDTO.ActionDTO> actionButtons = new ArrayList<>();
        
        int count = Math.min(tournaments.size(), 5);
        
        for (int i = 0; i < count; i++) {
            TournamentDto tournament = tournaments.get(i);
            response.append("🏆 ").append(tournament.getName()).append("\n");
            response.append("🎁 Giải thưởng: ").append(formatPrice(tournament.getPrize().doubleValue())).append("\n");
            response.append("📅 Thời gian: ").append(formatTournamentDate(tournament)).append("\n\n");
            
            actionButtons.add(new ChatbotResponseDTO.ActionDTO("Đăng ký " + tournament.getName(), "action", Map.of("action", "register_tournament_" + tournament.getTournamentId())));
        }
        
        actionButtons.add(new ChatbotResponseDTO.ActionDTO("Xem tất cả giải đấu", "action", Map.of("action", "view_all_tournaments")));
        
        return new ChatbotResponseDTO(response.toString(), actionButtons, sessionId, entities);
    }
    
    private String formatTournamentDate(TournamentDto tournament) {
        if (tournament.getStartDate() != null && tournament.getEndDate() != null) {
            return formatDateTime(tournament.getStartDate()) + " - " + formatDateTime(tournament.getEndDate());
        } else if (tournament.getStartDate() != null) {
            return "Từ " + formatDateTime(tournament.getStartDate());
        }
        return "Chưa xác định";
    }
    
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "Chưa xác định";
        
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }
    
    private String formatStatus(String status) {
        if (status == null) return "Chưa xác định";
        
        switch (status.toUpperCase()) {
            case "ACTIVE":
                return "Đang mở đăng ký";
            case "UPCOMING":
                return "Sắp diễn ra";
            case "ONGOING":
                return "Đang diễn ra";
            case "COMPLETED":
                return "Đã kết thúc";
            case "CANCELLED":
                return "Đã hủy";
            default:
                return status;
        }
    }
    
    private String formatPrice(Double price) {
        if (price == null) return "Miễn phí";
        return String.format("%,.0f VNĐ", price);
    }
}