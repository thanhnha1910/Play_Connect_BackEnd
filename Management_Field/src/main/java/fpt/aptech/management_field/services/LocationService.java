package fpt.aptech.management_field.services;

import fpt.aptech.management_field.mappers.LocationReviewMapper;
import fpt.aptech.management_field.models.Field;
import fpt.aptech.management_field.models.FieldType;
import fpt.aptech.management_field.models.Location;
import fpt.aptech.management_field.models.LocationReview;
import fpt.aptech.management_field.payload.dtos.BookingDTO;
import fpt.aptech.management_field.payload.dtos.FieldDTO;
import fpt.aptech.management_field.payload.dtos.FieldTypeDTO;
import fpt.aptech.management_field.payload.dtos.LocationReviewDTO;
import fpt.aptech.management_field.payload.response.FieldSummaryResponse;
import fpt.aptech.management_field.payload.response.LocationCardResponse;
import fpt.aptech.management_field.payload.response.LocationDetailResponse;
import fpt.aptech.management_field.payload.response.LocationMapResponse;
import fpt.aptech.management_field.repositories.BookingRepository;
import fpt.aptech.management_field.repositories.FieldRepository;
import fpt.aptech.management_field.repositories.LocationRepository;
import fpt.aptech.management_field.repositories.LocationReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private LocationReviewRepository locationReviewRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private BookingService bookingService;

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

    public List<LocationCardResponse> getAllLocationsForCards(String sortBy) {
        List<Location> locations;
        
        // Sort locations based on the sortBy parameter
        switch (sortBy.toLowerCase()) {
            case "rating":
                locations = locationRepository.findAllSortedByRating();
                break;
            case "popularity":
                // Calculate 30 days ago for popularity sorting
                java.time.Instant thirtyDaysAgo = java.time.Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS);
                locations = locationRepository.findAllSortedByPopularity(thirtyDaysAgo);
                break;
            default:
                // Default to rating if sortBy is not recognized
                locations = locationRepository.findAllSortedByRating();
                break;
        }
        
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

        // Temporarily set default values to avoid complex queries
        // TODO: Re-enable complex queries once database schema is confirmed
        response.setFieldCount(0);
        response.setAverageRating(null);
        response.setStartingPrice(null);
        response.setBookingCount(0L);
        response.setMainImageUrl(null);

        return response;
    }

    public LocationDetailResponse getLocationDetail(String locationSlug) {
        System.out.println("=== LOCATION SEARCH DEBUG ===");
        System.out.println("Searching for location with slug: " + locationSlug);
        
        // Try exact match first
        Location location = locationRepository.getLocationBySlug(locationSlug);
        System.out.println("Exact match result: " + (location != null ? "Found - " + location.getName() : "Not found"));
        
        if (location == null) {
            // Try case-insensitive search
            System.out.println("Trying case-insensitive search...");
            location = locationRepository.getLocationBySlugIgnoreCase(locationSlug);
            System.out.println("Case-insensitive match result: " + (location != null ? "Found - " + location.getName() : "Not found"));
        }
        
        if (location == null) {
            // Try native SQL query
            System.out.println("Trying native SQL query...");
            location = locationRepository.getLocationBySlugNative(locationSlug);
            System.out.println("Native SQL match result: " + (location != null ? "Found - " + location.getName() : "Not found"));
        }
        
        if (location == null) {
            // Debug: List all locations
            System.out.println("Location not found. Listing all available locations:");
            List<Location> allLocations = locationRepository.findAllLocations();
            System.out.println("Total locations in database: " + allLocations.size());
            for (Location loc : allLocations) {
                System.out.println("Available slug: '" + loc.getSlug() + "' - Name: " + loc.getName());
            }
            System.out.println("=== END DEBUG ===");
            return null;
        }
        
        System.out.println("Location found successfully: " + location.getName());
        System.out.println("=== END DEBUG ===");
        
        try {
            System.out.println("Starting to build response...");
            List<Field> locationFields = fieldRepository.getFieldsByLocationId(location.getLocationId());
            System.out.println("Found " + locationFields.size() + " fields for location");
            
            Map<FieldType, List<Field>> groupedByType = locationFields.stream()
                    .collect(Collectors.groupingBy(Field::getType));
            System.out.println("Grouped fields by " + groupedByType.size() + " types");
            
            List<FieldTypeDTO> typeDTOS = groupedByType.entrySet().stream()
                    .map(entry -> {
                        FieldType type = entry.getKey();
                        List<FieldDTO> fieldDTOs = entry.getValue().stream()
                                .map(field -> {
                                    try {
                                        // Get current date and next 7 days for booking data
                                        LocalDateTime startDate = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
                                        LocalDateTime endDate = startDate.plusDays(7).withHour(23).withMinute(59).withSecond(59);
                                        
                                        // Convert to Instant for the service call
                                        Instant startInstant = startDate.atZone(ZoneId.systemDefault()).toInstant();
                                        Instant endInstant = endDate.atZone(ZoneId.systemDefault()).toInstant();
                                        
                                        // Fetch bookings for this field
                                        List<BookingDTO> fieldBookings = bookingService.getBookingsByDate(startInstant, endInstant, field.getFieldId());
                                        
                                        // Create FieldDTO with bookings
                                        FieldDTO fieldDTO = new FieldDTO(
                                                field.getFieldId(),
                                                field.getName(),
                                                field.getDescription(),
                                                field.getHourlyRate()
                                        );
                                        fieldDTO.setBookings(fieldBookings);
                                        return fieldDTO;
                                    } catch (Exception e) {
                                        System.out.println("Error processing field " + field.getFieldId() + ": " + e.getMessage());
                                        e.printStackTrace();
                                        throw new RuntimeException("Error processing field", e);
                                    }
                                })
                                .collect(Collectors.toList());

                        return new FieldTypeDTO(
                                type.getName(),
                                type.getTeamCapacity(),
                                type.getMaxCapacity(),
                                fieldDTOs
                        );
                    }).toList();
            
            System.out.println("Created " + typeDTOS.size() + " field type DTOs");
            
            List<LocationReview> locationReviews = locationReviewRepository.findByLocationId(location.getLocationId());
            List<LocationReviewDTO> reviewDTOS = LocationReviewMapper.listToDTO(locationReviews);
            System.out.println("Found " + reviewDTOS.size() + " reviews");

            LocationDetailResponse response = new LocationDetailResponse();
            response.setName(location.getName());
            response.setAddress(location.getAddress());
            response.setFieldTypes(typeDTOS);
            response.setReviews(reviewDTOS);
            
            System.out.println("=== RESPONSE DEBUG ===");
            System.out.println("Response created with name: " + response.getName());
            System.out.println("Response address: " + response.getAddress());
            System.out.println("Field types count: " + (response.getFieldTypes() != null ? response.getFieldTypes().size() : "null"));
            System.out.println("Reviews count: " + (response.getReviews() != null ? response.getReviews().size() : "null"));
            System.out.println("Response object: " + (response != null ? "not null" : "null"));
            System.out.println("=== END RESPONSE DEBUG ===");

            return response;
        } catch (Exception e) {
            System.out.println("ERROR in getLocationDetail: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to build location detail response", e);
        }
    }
}
