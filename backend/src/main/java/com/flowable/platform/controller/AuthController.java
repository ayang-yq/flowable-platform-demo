package com.flowable.platform.controller;

import com.flowable.platform.dto.ApiResponse;
import com.flowable.platform.dto.LoginRequest;
import com.flowable.platform.dto.LoginResponse;
import com.flowable.platform.dto.UserInfoDTO;
import com.flowable.platform.entity.Role;
import com.flowable.platform.entity.Tenant;
import com.flowable.platform.entity.User;
import com.flowable.platform.repository.TenantRepository;
import com.flowable.platform.repository.UserRepository;
import com.flowable.platform.service.JwtTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthController(UserRepository userRepository,
                         TenantRepository tenantRepository,
                         PasswordEncoder passwordEncoder,
                         JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for user: {} in tenant: {}", request.getUsername(), request.getTenantCode());

        // Find tenant by code
        Tenant tenant = tenantRepository.findActiveByCode(request.getTenantCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid tenant"));

        // Find user by tenant and username
        User user = userRepository.findActiveByTenantIdAndUsername(tenant.getId(), request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for user: {} in tenant: {}", request.getUsername(), request.getTenantCode());
            throw new IllegalArgumentException("Invalid credentials");
        }

        log.info("Successful login for user: {} in tenant: {}", user.getUsername(), tenant.getCode());

        // Generate JWT token
        String token = jwtTokenService.generateToken(user.getUsername(), tenant.getId().toString());

        // Build response
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setTenantCode(tenant.getCode());
        response.setTenantId(tenant.getId().toString());
        response.setDisplayName(user.getDisplayName());

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        response.setRoles(roleNames);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoDTO>> me(@RequestHeader(value = "X-Tenant-Id") String tenantId) {
        // Get current username from security context
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Look up the User by username and tenant
        User user = userRepository.findActiveByTenantIdAndUsername(UUID.fromString(tenantId), username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Look up the Tenant by ID
        Tenant tenant = tenantRepository.findById(UUID.fromString(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        // Map roles from Role entities to role names
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // Build and return UserInfoDTO
        UserInfoDTO userInfo = new UserInfoDTO(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail(),
                tenant.getCode(),
                tenant.getId().toString(),
                roleNames,
                user.getAvatarUrl()
        );

        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
