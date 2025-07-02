package fpt.aptech.management_field.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationCardResponse {
    private Long locationId;
    private String locationName;
    private String address;
    private String mainImageUrl;
    private int fieldCount;
    private Double averageRating;
    private BigDecimal startingPrice;
    private Long bookingCount;
}