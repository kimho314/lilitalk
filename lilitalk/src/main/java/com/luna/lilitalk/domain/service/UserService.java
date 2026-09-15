package com.luna.lilitalk.domain.service;

import com.luna.lilitalk.domain.dto.UserDto;
import com.luna.lilitalk.domain.dto.UserDto.CreateUserRequest;
import com.luna.lilitalk.domain.dto.UserDto.LoginRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    // 사용자 관리
    UserDto createUser(CreateUserRequest request);

    UserDto login(LoginRequest request);

    UserDto getUserById(Long userId);

    Page<UserDto> searchUsers(String query, Pageable pageable);

    // 사용자 상태
    UserDto updateLastSeen(Long userId);
}
