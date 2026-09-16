package com.luna.lilitalk.persistence.repository;

import com.luna.lilitalk.domain.model.ChatRoom;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
            SELECT DISTINCT cr FROM ChatRoom cr 
            JOIN ChatRoomMember crm ON cr.id = crm.chatRoom.id 
            WHERE crm.user.id = :userId AND crm.isActive = true AND cr.isActive = true
            ORDER BY cr.updatedAt DESC
        """)
    Page<ChatRoom> findUserChatRooms(Long userId, Pageable pageable);

    List<ChatRoom> findByIsActiveTrueOrderByCreatedAtDesc();

    List<ChatRoom> findByNameContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(String name);
}
