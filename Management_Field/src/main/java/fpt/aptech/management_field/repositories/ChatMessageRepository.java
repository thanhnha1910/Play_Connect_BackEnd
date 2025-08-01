package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomIdOrderBySentAtAsc(Long chatRoomId);
    List<ChatMessage> findByChatRoomIdOrderBySentAtDesc(Long chatRoomId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM ChatMessage cm WHERE cm.chatRoom.id = :chatRoomId")
    void deleteByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}