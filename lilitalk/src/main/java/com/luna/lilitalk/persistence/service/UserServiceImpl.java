package com.luna.lilitalk.persistence.service;

import com.luna.lilitalk.domain.dto.UserDto;
import com.luna.lilitalk.domain.dto.UserDto.CreateUserRequest;
import com.luna.lilitalk.domain.dto.UserDto.LoginRequest;
import com.luna.lilitalk.domain.model.User;
import com.luna.lilitalk.domain.service.UserService;
import com.luna.lilitalk.persistence.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {


    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private UserDto.UserDataDto userToDto(User user) {
        return new UserDto.UserDataDto(
            user.getId(),
            user.getUsername(),
            // 이거는 구현이 안되어 있다.
            user.getDisplayName(),
            user.getProfileImageUrl(),
            //
            user.getStatus(),
            user.getIsActive(),
            user.getLastSeenAt(),
            user.getCreatedAt()
        );
    }

    private String hashPassword(String password) {
        byte[] bytes;
        try {
            bytes = MessageDigest.getInstance("SHA-256")
                .digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return HexFormat.of().formatHex(bytes);
    }

    @Transactional
    @Override
    public UserDto.UserDataDto createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다: ${request.username}");
        }

        var user = new User(
            request.username(),
            hashPassword(request.password()),
            request.displayName()
        );

        var savedUser = userRepository.save(user);
        return userToDto(savedUser);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto.UserDataDto login(LoginRequest request) {
        var user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없거나 비밀번호가 일치하지 않습니다."));

        if (!Objects.equals(user.getPassword(), hashPassword(request.password()))) {
            throw new IllegalArgumentException("사용자를 찾을 수 없거나 비밀번호가 일치하지 않습니다.");
        }

        return userToDto(user);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto.UserDataDto getUserById(Long userId) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: $userId"));
        return userToDto(user);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<UserDto.UserDataDto> searchUsers(String query, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(query, pageable);

        return users.getContent().stream()
            .map(this::userToDto)
            .collect(Collectors.collectingAndThen(
                Collectors.toList(),
                list -> new PageImpl<>(list, pageable, users.getTotalElements())
            ));
    }

    @Transactional
    @Override
    public UserDto.UserDataDto updateLastSeen(Long userId) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: $userId"));

        var now = LocalDateTime.now();
        userRepository.updateLastSeenAt(userId, now);

        User copy = new User(
            user.getId(),
            user.getUsername(),
            user.getPassword(),
            user.getDisplayName(),
            user.getProfileImageUrl(),
            user.getStatus(),
            user.getIsActive(),
            now,
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
        return userToDto(copy);
    }
}
