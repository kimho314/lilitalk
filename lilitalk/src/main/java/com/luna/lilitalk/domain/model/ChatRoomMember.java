package com.luna.lilitalk.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "chat_room_members",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"chat_room_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_chat_room_member_user_id", columnList = "user_id"),
        @Index(name = "idx_chat_room_member_chat_room_id", columnList = "chat_room_id"),
        @Index(name = "idx_chat_room_member_active", columnList = "is_active"),
        @Index(name = "idx_chat_room_member_role", columnList = "role")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class ChatRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MemberRole role = MemberRole.MEMBER;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column
    private Long lastReadMessageId;

    @Column(nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Column
    private LocalDateTime leftAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ChatRoomMember() {
    }

    public ChatRoomMember(
        Long id,
        ChatRoom chatRoom,
        User user,
        MemberRole role,
        Boolean isActive,
        Long lastReadMessageId,
        LocalDateTime joinedAt,
        LocalDateTime leftAt,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.chatRoom = chatRoom;
        this.user = user;
        this.role = role;
        this.isActive = isActive;
        this.lastReadMessageId = lastReadMessageId;
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
        this.createdAt = createdAt;
    }

    public ChatRoomMember(
        ChatRoom savedRoom,
        User creator,
        MemberRole memberRole
    ) {
        this.chatRoom = savedRoom;
        this.user = creator;
        this.role = memberRole;
    }

    public Long getId() {
        return id;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public User getUser() {
        return user;
    }

    public MemberRole getRole() {
        return role;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public Long getLastReadMessageId() {
        return lastReadMessageId;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public LocalDateTime getLeftAt() {
        return leftAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setRole(MemberRole role) {
        this.role = role;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public void setLastReadMessageId(Long lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public void setLeftAt(LocalDateTime leftAt) {
        this.leftAt = leftAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChatRoomMember that = (ChatRoomMember) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ChatRoomMember{" +
            "id=" + id +
            ", chatRoom=" + chatRoom +
            ", user=" + user +
            ", role=" + role +
            ", isActive=" + isActive +
            ", lastReadMessageId=" + lastReadMessageId +
            ", joinedAt=" + joinedAt +
            ", leftAt=" + leftAt +
            ", createdAt=" + createdAt +
            '}';
    }
}
