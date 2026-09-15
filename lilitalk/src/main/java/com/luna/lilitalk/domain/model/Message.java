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
import java.time.LocalDateTime;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "messages",
    indexes = {
        @Index(name = "idx_message_chat_room_id", columnList = "chat_room_id"),
        @Index(name = "idx_message_sender_id", columnList = "sender_id"),
        @Index(name = "idx_message_created_at", columnList = "created_at"),
        @Index(name = "idx_message_room_time", columnList = "chat_room_id,created_at"),
        @Index(name = "idx_message_room_sequence", columnList = "chat_room_id,sequence_number")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MessageType type = MessageType.TEXT;

    @Column(columnDefinition = "TEXT")
    @Nullable
    private String content;

    @Column(nullable = false)
    private Boolean isEdited = false;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Column(nullable = false)
    private Long sequenceNumber = 0L;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime editedAt;

    public Message() {
    }

    public Message(
        Long id,
        ChatRoom chatRoom,
        User sender,
        MessageType type,
        @Nullable String content,
        Boolean isEdited,
        Boolean isDeleted,
        Long sequenceNumber,
        LocalDateTime createdAt,
        LocalDateTime editedAt
    ) {
        this.id = id;
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.type = type;
        this.content = content;
        this.isEdited = isEdited;
        this.isDeleted = isDeleted;
        this.sequenceNumber = sequenceNumber;
        this.createdAt = createdAt;
        this.editedAt = editedAt;
    }

    public Long getId() {
        return id;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public User getSender() {
        return sender;
    }

    public MessageType getType() {
        return type;
    }

    public @Nullable String getContent() {
        return content;
    }

    public Boolean getEdited() {
        return isEdited;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getEditedAt() {
        return editedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public void setContent(@Nullable String content) {
        this.content = content;
    }

    public void setEdited(Boolean edited) {
        isEdited = edited;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public void setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setEditedAt(LocalDateTime editedAt) {
        this.editedAt = editedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Message message = (Message) o;
        return Objects.equals(id, message.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Message{" +
            "id=" + id +
            ", chatRoom=" + chatRoom +
            ", sender=" + sender +
            ", type=" + type +
            ", content='" + content + '\'' +
            ", isEdited=" + isEdited +
            ", isDeleted=" + isDeleted +
            ", sequenceNumber=" + sequenceNumber +
            ", createdAt=" + createdAt +
            ", editedAt=" + editedAt +
            '}';
    }
}
