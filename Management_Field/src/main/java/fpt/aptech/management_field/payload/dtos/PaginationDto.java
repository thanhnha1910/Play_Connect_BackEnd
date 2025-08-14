package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for pagination information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationDto {
    private long totalItems;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}