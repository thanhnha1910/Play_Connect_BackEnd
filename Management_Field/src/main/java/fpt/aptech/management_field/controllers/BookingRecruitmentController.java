package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.payload.dtos.BookingRecruitmentDTO;
import fpt.aptech.management_field.payload.dtos.RecruitmentPostRequestDTO;
import fpt.aptech.management_field.models.BookingRequirement;
import fpt.aptech.management_field.services.BookingRecruitmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/recruitments")
public class BookingRecruitmentController {

    private final BookingRecruitmentService bookingRecruitmentService;

    public BookingRecruitmentController(BookingRecruitmentService service) {
        this.bookingRecruitmentService = service;
    }

    // ✅ Create with minimal user input
    @PostMapping
    public ResponseEntity<BookingRecruitmentDTO> create(@RequestBody RecruitmentPostRequestDTO dto) {
        BookingRequirement saved = bookingRecruitmentService.createFromUser(
                dto.getUserId(),
                dto.getPeopleNeeded(),
                dto.getMessage()
        );
        return ResponseEntity.ok(toDTO(saved));
    }

    // ✅ Get all
    @GetMapping
    public ResponseEntity<List<BookingRecruitmentDTO>> getAll() {
        List<BookingRequirement> recruitments = bookingRecruitmentService.findAll();
        List<BookingRecruitmentDTO> dtos = recruitments.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // ✅ Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        bookingRecruitmentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ DTO converter
    private BookingRecruitmentDTO toDTO(BookingRequirement r) {
        BookingRecruitmentDTO dto = new BookingRecruitmentDTO();
        dto.setId(r.getId()); // ✅ Fix: Set the ID
        dto.setUserName(r.getUserName());
        dto.setPhone(r.getPhone());
        dto.setFieldNumber(r.getFieldNumber());
        dto.setFieldLocation(r.getFieldLocation());
        dto.setPlayTime(r.getPlayTime());
        dto.setPeopleNeeded(r.getPeopleNeeded());
        dto.setMessage(r.getMessage());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }
    // ✅ Get all recruitment posts by a specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingRecruitmentDTO>> getByUserId(@PathVariable Long userId) {
        System.out.println("Searching recruitment for user: " + userId);
        List<BookingRequirement> recruitments = bookingRecruitmentService.findByUserId(userId);
        List<BookingRecruitmentDTO> dtos = recruitments.stream()
                .map(recruitment -> {
                    BookingRecruitmentDTO dto = toDTO(recruitment);

                    // Count participants per recruitment
                    int participantCount = recruitment.getParticipants() != null
                            ? recruitment.getParticipants().size()
                            : 0;

                    dto.setParticipantCount(participantCount);
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
