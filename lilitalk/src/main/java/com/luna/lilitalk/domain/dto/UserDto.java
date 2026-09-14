package com.luna.lilitalk.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class UserDto {

    public record UserDataDto(
        Long id,
        String username,
        String displayName,
        String profileImageUrl,
        String status,
        Boolean isActive,
        LocalDateTime lastSeenAt,
        LocalDateTime createdAt
    ) {

    }

    public record CreateUserRequest(
        @NotBlank(message = "사용자명은 필수입니다")
        @Size(min = 3, max = 20, message = "사용자명은 3-20자 사이여야 합니다")
        String username,

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 3, message = "비밀번호는 최소 3자 이상이어야 합니다")
        String password,

        @NotBlank(message = "표시 이름은 필수입니다")
        @Size(min = 1, max = 50, message = "표시 이름은 1-50자 사이여야 합니다")
        String displayName
    ) {

    }

    public record LoginRequest(
        @NotBlank(message = "사용자명은 필수입니다")
        String username,

        @NotBlank(message = "비밀번호는 필수입니다")
        String password
    ) {

    }
}
