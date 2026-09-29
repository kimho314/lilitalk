package com.luna.lilitalk.api.controller;

import com.luna.lilitalk.domain.dto.UserDto;
import com.luna.lilitalk.domain.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto.UserDataDto> register(
        @Valid @RequestBody UserDto.CreateUserRequest request
    ) {
        var user = userService.createUser(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto.UserDataDto> login(
        @Valid @RequestBody UserDto.LoginRequest request) {
        var user = userService.login(request);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto.UserDataDto> getUser(@PathVariable Long id) {
        var user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto.UserDataDto> getCurrentUser(@RequestParam Long userId) {
        var user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserDto.UserDataDto>> searchUsers(
        @RequestParam String username,
        @PageableDefault(size = 10) Pageable pageable
    ) {
        var users = userService.searchUsers(username, pageable);
        return ResponseEntity.ok(users);
    }
}
