package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for owner booking data response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerBookingDto {
    private Long id;
    private String customerName;
    private String status;
    private Instant startTime;
    private Instant endTime;
    private double price;
    private double ownerAmount; // Số tiền owner thực nhận sau khi trừ 5% cho admin
    private FieldInfo field;
    private FacilityInfo facility;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldInfo {
        private Long id;
        private String name;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacilityInfo {
        private Long id;
        private String name;
    }
}