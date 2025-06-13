package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.Field;
import fpt.aptech.management_field.models.Location;
import fpt.aptech.management_field.payload.response.FieldSummaryResponse;
import fpt.aptech.management_field.payload.response.LocationCardResponse;
import fpt.aptech.management_field.payload.response.LocationMapResponse;
import fpt.aptech.management_field.repositories.FieldRepository;
import fpt.aptech.management_field.repositories.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private FieldRepository fieldRepository;

    public List<LocationMapResponse> searchLocationsForMap(BigDecimal latitude,
                                                           BigDecimal longitude,
                                                           Double radiusKm,
                                                           Integer zoomLevel, // zoomLevel hiện chưa được sử dụng, có thể dùng cho clustering sau này
                                                           Long typeId,
                                                           Long categoryId,
                                                           Integer minHourlyRate,
                                                           Integer maxHourlyRate) {

        List<Location> locations;

        // Check if any filters are applied for fields within locations
        boolean hasFilters = typeId != null || categoryId != null ||
                minHourlyRate != null || maxHourlyRate != null;

        if (hasFilters) {
            locations = locationRepository.findLocationsWithinRadiusWithFilters(
                    latitude, longitude, radiusKm, typeId, categoryId, minHourlyRate, maxHourlyRate);
        } else {
            locations = locationRepository.findLocationsWithinRadius(latitude, longitude, radiusKm);
        }

        return locations.stream().map(this::convertToLocationMapResponse).collect(Collectors.toList());
    }

    public List<FieldSummaryResponse> getFieldsByLocation(Long locationId,
                                                          Long typeId,
                                                          Long categoryId,
                                                          Integer minHourlyRate,
                                                          Integer maxHourlyRate) {

        // Verify location exists
        locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + locationId)); // Cân nhắc dùng custom exception

        // Build dynamic query using Specification
        // Bắt đầu Specification với điều kiện bắt buộc là locationId
        Specification<Field> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("location").get("locationId"), locationId);

        // Apply other optional filters by chaining .and()
        if (typeId != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("type").get("typeId"), typeId));
        }

        if (categoryId != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("category").get("categoryId"), categoryId));
        }

        if (minHourlyRate != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("hourlyRate"), minHourlyRate));
        }

        if (maxHourlyRate != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("hourlyRate"), maxHourlyRate));
        }

        List<Field> fields = fieldRepository.findAll(spec); // Dòng này giờ sẽ không còn warning nữa nếu FieldRepository kế thừa JpaSpecificationExecutor

        return fields.stream().map(this::convertToFieldSummaryResponse).collect(Collectors.toList());
    }

    public List<LocationCardResponse> getAllLocationsForCards() {
        List<Location> locations = locationRepository.findAll();
        return locations.stream().map(this::convertToLocationCardResponse).collect(Collectors.toList());
    }

    private LocationMapResponse convertToLocationMapResponse(Location location) {
        LocationMapResponse response = new LocationMapResponse();
        response.setLocationId(location.getLocationId());
        response.setName(location.getName());
        response.setAddress(location.getAddress());
        response.setLatitude(location.getLatitude());
        response.setLongitude(location.getLongitude());

        // Get field count
        Integer fieldCount = locationRepository.countFieldsByLocationId(location.getLocationId());
        response.setFieldCount(fieldCount != null ? fieldCount : 0); // Xử lý trường hợp count trả về null

        // Get average rating (may be null if no reviews)
        BigDecimal averageRating = locationRepository.getAverageRatingByLocationId(location.getLocationId());
        response.setAverageRating(averageRating);

        // Set thumbnail (you can implement logic to get the first field's image or location image)
        response.setThumbnailImageUrl(null); // TODO: Implement as needed
        // Ví dụ:
        // if (location.getFields() != null && !location.getFields().isEmpty()) {
        //    // Lấy ảnh từ field đầu tiên hoặc một logic nào đó
        //    response.setThumbnailImageUrl(location.getFields().get(0).getSomeImageUrlField());
        // } else if (location.getLocationImageUrl() != null) {
        //    response.setThumbnailImageUrl(location.getLocationImageUrl());
        // }


        return response;
    }

    private FieldSummaryResponse convertToFieldSummaryResponse(Field field) {
        FieldSummaryResponse response = new FieldSummaryResponse();
        response.setFieldId(field.getFieldId());
        response.setFieldName(field.getDescription()); // Using description as field name
        response.setHourlyRate(field.getHourlyRate());
        if (field.getType() != null) {
            response.setTypeName(field.getType().getName());
        }
        if (field.getCategory() != null) {
            response.setCategoryName(field.getCategory().getName());
        }
        response.setThumbnailImageUrl(null); // TODO: Implement as needed
        // Ví dụ:
        // response.setThumbnailImageUrl(field.getSomeImageUrlField());

        return response;
    }

    private LocationCardResponse convertToLocationCardResponse(Location location) {
        LocationCardResponse response = new LocationCardResponse();
        response.setLocationId(location.getLocationId());
        response.setLocationName(location.getName());
        response.setAddress(location.getAddress());
        
        // Get field count
        Integer fieldCount = locationRepository.countFieldsByLocationId(location.getLocationId());
        response.setFieldCount(fieldCount != null ? fieldCount : 0);
        
        // Get average rating (may be null if no reviews)
        BigDecimal averageRating = locationRepository.getAverageRatingByLocationId(location.getLocationId());
        response.setAverageRating(averageRating != null ? averageRating.doubleValue() : null);
        
        // Get starting price (minimum hourly rate)
        Integer minHourlyRate = locationRepository.getMinimumHourlyRateByLocationId(location.getLocationId());
        response.setStartingPrice(minHourlyRate != null ? BigDecimal.valueOf(minHourlyRate) : null);
        
        // Set main image URL (TODO: Implement as needed)
        response.setMainImageUrl(null); // TODO: Implement logic to get main image
        
        return response;
    }
}
