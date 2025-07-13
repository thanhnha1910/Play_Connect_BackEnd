package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Get all messages between two users (regardless of who sent)
    List<Message> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByTimestampAsc(
            Long senderId1, Long receiverId1, Long senderId2, Long receiverId2
    );
    List<Message> findBySenderIdAndReceiverIdAndIsReadFalse(Long senderId, Long receiverId);

}
