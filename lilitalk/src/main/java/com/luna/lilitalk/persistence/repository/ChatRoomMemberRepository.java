package com.luna.lilitalk.persistence.repository;

import com.luna.lilitalk.domain.model.ChatRoomMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    List<ChatRoomMember> findByChatRoomIdAndIsActiveTrue(Long chatRoomId);

    Optional<ChatRoomMember> findByChatRoomIdAndUserIdAndIsActiveTrue(Long chatRoomId, Long userId);

    @Query("SELECT COUNT(crm) FROM ChatRoomMember crm WHERE crm.chatRoom.id = :chatRoomId AND crm.isActive = true")
    Long countActiveMembersInRoom(Long chatRoomId);

    @Modifying
    @Query("""
             UPDATE ChatRoomMember crm
             SET crm.isActive = false, crm.leftAt = CURRENT_TIMESTAMP
             WHERE crm.chatRoom.id = :chatRoomId AND crm.user.id = :userId
        """)
    void leaveChatRoom(Long chatRoomId, Long userId);

    Boolean existsByChatRoomIdAndUserIdAndIsActiveTrue(Long chatRoomId, Long userId);
}
