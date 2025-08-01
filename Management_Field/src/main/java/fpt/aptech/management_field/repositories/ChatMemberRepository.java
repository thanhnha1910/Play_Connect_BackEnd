package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.ChatMember;
import fpt.aptech.management_field.models.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {

    List<ChatMember> findByChatRoomId(Long chatRoomId);

    Optional<ChatMember> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    List<ChatMember> findByUserId(Long userId);

    @Query("SELECT cm FROM ChatMember cm WHERE cm.chatRoom.id = :chatRoomId AND cm.isActive = true")
    List<ChatMember> findActiveMembersByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    @Query("SELECT cm FROM ChatMember cm WHERE cm.user.id = :userId AND cm.isActive = true")
    List<ChatMember> findActiveMembersByUserId(@Param("userId") Long userId);

    @Query("SELECT cm.chatRoom FROM ChatMember cm WHERE cm.user.id = :userId AND cm.isActive = true")
    List<ChatRoom> findActiveChatRoomsByUserId(@Param("userId") Long userId);

    boolean existsByChatRoomIdAndUserId(Long chatRoomId, Long userId);
    
    int countByChatRoomIdAndIsActiveTrue(Long chatRoomId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM ChatMember cm WHERE cm.chatRoom.id = :chatRoomId")
    void deleteByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}