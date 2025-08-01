package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // Tìm tất cả phòng chat mà một người dùng là thành viên
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.members m WHERE m.user.id = :userId AND m.isActive = true")
    List<ChatRoom> findByMembersUserId(Long userId);

    // Tìm phòng chat theo tên (dùng để kiểm tra trùng lặp)
    Optional<ChatRoom> findByName(String name);

    // Lấy danh sách phòng chat mà người dùng tham gia
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.members m WHERE m.user.id = :userId AND m.isActive = true")
    List<ChatRoom> findByUserId(@Param("userId") Long userId);
}