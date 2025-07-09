package salex.messenger.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import salex.messenger.entity.Message;
import salex.messenger.entity.User;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // TODO: пагинация лучше
    @Query(
            """
                        SELECT m FROM Message m
                        WHERE (m.sender.id = :id1 AND m.receiver.id = :id2) OR (m.sender.id = :id2 AND m.receiver.id = :id1)
                        ORDER BY m.createdAt ASC
                    """)
    List<Message> getChatHistory(@Param("id1") Long firstUserId, @Param("id2") Long secondUserId);

    // Получить все чаты для конкретного пользователя (либо он писал, либо ему писали хотя бы раз)
    @Query(
            """
            SELECT DISTINCT u FROM User u
            WHERE u.id IN (
                SELECT m.receiver.id FROM Message m WHERE m.sender.id = :userId
                UNION
                SELECT m.sender.id FROM Message m WHERE m.receiver.id = :userId
            )
            """)
    List<User> getAllChatPartners(@Param("userId") Long currentUserId);

    // Удалить чат
    @Modifying
    @Transactional
    @Query(
            """
            DELETE FROM Message m
            WHERE (m.sender.id = :id1 AND m.receiver.id = :id2) OR (m.sender.id = :id2 AND m.receiver.id = :id1)
            """)
    void removeChat(@Param("id1") Long firstId, @Param("id2") Long secondId);
}
