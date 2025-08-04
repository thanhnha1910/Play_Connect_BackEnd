# DEBUG TASK: Lỗi tạo Draft Match (500 Internal Server Error)

## Mô tả vấn đề
- User báo cáo lỗi không thể tạo draft match
- Server trả về 500 Internal Server Error
- Frontend hiển thị "Có lỗi xảy ra khi tạo kèo. Vui lòng thử lại."

## Thông tin đã thu thập

### 1. Controller Layer
- File: `DraftMatchController.java`
- Method: `createDraftMatch()` (line 47-86)
- Có generic exception handling với log error

### 2. Service Layer
- File: `DraftMatchService.java`
- Method: `createDraftMatch()` (line 64-95)
- Logic:
  1. Tìm user theo ID
  2. Tạo DraftMatch object
  3. Convert requiredTags từ List<String> sang JSON string
  4. Save vào database
  5. Convert sang DTO
  6. Gửi WebSocket message

### 3. Request Structure
- Frontend gửi:
```javascript
{
  sportType: '',
  locationDescription: '',
  estimatedStartTime: '',
  estimatedEndTime: '',
  slotsNeeded: 1,
  skillLevel: '',
  requiredTags: []
}
```

### 4. Validation
- `CreateDraftMatchRequest` có các validation:
  - @NotBlank cho sportType, locationDescription, skillLevel
  - @NotNull cho estimatedStartTime, estimatedEndTime, slotsNeeded
  - @Future cho thời gian
  - @Min/@Max cho slotsNeeded
  - Custom validator cho endTime > startTime

## Các nguyên nhân có thể

### 1. JsonProcessingException
- Lỗi khi convert requiredTags sang JSON
- Code: `objectMapper.writeValueAsString(request.getRequiredTags())`
- Có thể do requiredTags null hoặc chứa dữ liệu không hợp lệ

### 2. Database Constraint Violation
- Lỗi khi save vào database
- Có thể do:
  - Foreign key constraint (creator_user_id)
  - Column length exceeded
  - Null constraint violation

### 3. WebSocket Error
- Lỗi khi gửi message qua WebSocket
- Code: `messagingTemplate.convertAndSend()`

### 4. DateTime Format Issue
- Frontend gửi datetime string không đúng format
- Backend expect LocalDateTime

## Các bước debug cần thực hiện

### Bước 1: Thêm detailed logging
```java
@Transactional
public DraftMatchDto createDraftMatch(CreateDraftMatchRequest request, Long creatorUserId) {
    log.info("[DEBUG] Creating draft match for user: {}", creatorUserId);
    log.info("[DEBUG] Request data: {}", request);
    
    Optional<User> userOpt = userRepository.findById(creatorUserId);
    if (userOpt.isEmpty()) {
        log.error("[DEBUG] User not found: {}", creatorUserId);
        throw new RuntimeException("User not found");
    }
    log.info("[DEBUG] User found: {}", userOpt.get().getUsername());
    
    DraftMatch draftMatch = new DraftMatch();
    draftMatch.setCreator(userOpt.get());
    draftMatch.setSportType(request.getSportType());
    draftMatch.setLocationDescription(request.getLocationDescription());
    draftMatch.setEstimatedStartTime(request.getEstimatedStartTime());
    draftMatch.setEstimatedEndTime(request.getEstimatedEndTime());
    draftMatch.setSlotsNeeded(request.getSlotsNeeded());
    draftMatch.setSkillLevel(request.getSkillLevel());
    
    log.info("[DEBUG] DraftMatch object created, converting tags...");
    
    // Convert tags list to JSON string
    try {
        log.info("[DEBUG] Required tags: {}", request.getRequiredTags());
        String tagsJson = objectMapper.writeValueAsString(request.getRequiredTags());
        log.info("[DEBUG] Tags JSON: {}", tagsJson);
        draftMatch.setRequiredTags(tagsJson);
    } catch (JsonProcessingException e) {
        log.error("[DEBUG] Error processing required tags: {}", e.getMessage(), e);
        throw new RuntimeException("Error processing required tags", e);
    }
    
    log.info("[DEBUG] Saving draft match to database...");
    try {
        draftMatch = draftMatchRepository.save(draftMatch);
        log.info("[DEBUG] Draft match saved with ID: {}", draftMatch.getId());
    } catch (Exception e) {
        log.error("[DEBUG] Error saving draft match: {}", e.getMessage(), e);
        throw e;
    }
    
    log.info("[DEBUG] Converting to DTO...");
    DraftMatchDto dto = convertToDto(draftMatch, creatorUserId);
    
    log.info("[DEBUG] Sending WebSocket message...");
    try {
        messagingTemplate.convertAndSend("/topic/draft-match/" + draftMatch.getId(), dto);
        log.info("[DEBUG] WebSocket message sent successfully");
    } catch (Exception e) {
        log.error("[DEBUG] Error sending WebSocket message: {}", e.getMessage(), e);
        // Don't throw here, just log the error
    }
    
    log.info("[DEBUG] Draft match creation completed successfully");
    return dto;
}
```

### Bước 2: Kiểm tra database schema
- Verify các column constraints
- Check foreign key relationships
- Ensure column sizes are adequate

### Bước 3: Test với data cụ thể
- Tạo unit test với data giống như frontend gửi
- Test với requiredTags = null, empty array, array có data

### Bước 4: Kiểm tra datetime serialization
- Verify format datetime từ frontend
- Check timezone handling

### Bước 5: Monitor logs real-time
- Chạy backend với debug logging
- Thử tạo draft match từ frontend
- Quan sát logs để xác định điểm lỗi chính xác

## Status
- [ ] Thêm detailed logging
- [ ] Test với data cụ thể
- [ ] Kiểm tra database constraints
- [ ] Monitor logs real-time
- [ ] Fix root cause
- [ ] Verify fix works

## Notes
- Lỗi xảy ra ở production environment
- Cần test cẩn thận trước khi deploy fix
- Có thể cần rollback nếu fix gây ra vấn đề khác