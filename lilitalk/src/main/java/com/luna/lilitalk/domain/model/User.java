package com.luna.lilitalk.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "app_users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    @NotBlank
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(length = 500)
    @Nullable
    private String profileImageUrl;

    @Column(length = 50)
    @Nullable
    private String status;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column
    private LocalDateTime lastSeenAt = null;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public User() {
    }

    public User(
        String username,
        String hashPassword,
        String displayName
    ) {
        this.username = username;
        this.password = hashPassword;
        this.displayName = displayName;
    }

    public User(
        Long id,
        String username,
        String password,
        String displayName,
        @Nullable String profileImageUrl,
        @Nullable String status,
        Boolean isActive,
        LocalDateTime lastSeenAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.profileImageUrl = profileImageUrl;
        this.status = status;
        this.isActive = isActive;
        this.lastSeenAt = lastSeenAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public @Nullable String getProfileImageUrl() {
        return profileImageUrl;
    }

    public @Nullable String getStatus() {
        return status;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setProfileImageUrl(@Nullable String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setStatus(@Nullable String status) {
        this.status = status;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "User{" +
            "id=" + id +
            ", username='" + username + '\'' +
            ", password='" + password + '\'' +
            ", displayName='" + displayName + '\'' +
            ", profileImageUrl='" + profileImageUrl + '\'' +
            ", status='" + status + '\'' +
            ", isActive=" + isActive +
            ", lastSeenAt=" + lastSeenAt +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            '}';
    }
}
