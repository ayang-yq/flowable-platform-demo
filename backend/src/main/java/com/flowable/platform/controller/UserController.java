package com.flowable.platform.controller;

import com.flowable.platform.dto.ApiResponse;
import com.flowable.platform.dto.UserDTO;
import com.flowable.platform.dto.CreateUserRequest;
import com.flowable.platform.dto.UpdateUserRequest;
import com.flowable.platform.entity.User;
import com.flowable.platform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> listUsers() {
        List<UserDTO> users = userService.listUsers().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUser(@PathVariable UUID id) {
        User user = userService.getUserByIdOrThrow(id);
        return ResponseEntity.ok(ApiResponse.success(toDTO(user)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(
                request.getUsername(), request.getEmail(), request.getPassword(),
                request.getFirstName(), request.getLastName(), request.getPhone(),
                request.getRoleCodes(), request.getDepartmentCodes());
        return ResponseEntity.ok(ApiResponse.success(toDTO(user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable UUID id,
                                                            @Valid @RequestBody UpdateUserRequest request) {
        User user = userService.updateUser(id,
                request.getEmail(), request.getFirstName(), request.getLastName(),
                request.getPhone(), request.getDisplayName(), request.getLocale(),
                request.getTimezone(), request.getRoleCodes(), request.getDepartmentCodes());
        return ResponseEntity.ok(ApiResponse.success(toDTO(user)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setDisplayName(user.getDisplayName());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setPhone(user.getPhone());
        dto.setLocale(user.getLocale());
        dto.setTimezone(user.getTimezone());
        dto.setIsActive(user.getIsActive());
        dto.setEmailVerified(user.getEmailVerified());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setCreatedAt(user.getCreatedAt());
        if (user.getRoles() != null) {
            dto.setRoleCodes(user.getRoles().stream()
                    .map(r -> r.getCode())
                    .collect(Collectors.toSet()));
        }
        if (user.getDepartments() != null) {
            dto.setDepartmentCodes(user.getDepartments().stream()
                    .map(d -> d.getCode())
                    .collect(Collectors.toSet()));
        }
        return dto;
    }
}
