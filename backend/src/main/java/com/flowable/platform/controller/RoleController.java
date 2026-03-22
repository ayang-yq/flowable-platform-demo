package com.flowable.platform.controller;

import com.flowable.platform.dto.ApiResponse;
import com.flowable.platform.dto.RoleDTO;
import com.flowable.platform.dto.CreateRoleRequest;
import com.flowable.platform.entity.Role;
import com.flowable.platform.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleDTO>>> listRoles() {
        List<RoleDTO> roles = roleService.listRoles().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleDTO>> getRole(@PathVariable UUID id) {
        Role role = roleService.getRoleByIdOrThrow(id);
        return ResponseEntity.ok(ApiResponse.success(toDTO(role)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleDTO>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        Role role = roleService.createRole(
                request.getName(), request.getCode(), request.getDescription(), request.getPermissions());
        return ResponseEntity.ok(ApiResponse.success(toDTO(role)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleDTO>> updateRole(@PathVariable UUID id,
                                                            @Valid @RequestBody CreateRoleRequest request) {
        Role role = roleService.updateRole(id,
                request.getName(), request.getDescription(), request.getPermissions());
        return ResponseEntity.ok(ApiResponse.success(toDTO(role)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private RoleDTO toDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setCode(role.getCode());
        dto.setDescription(role.getDescription());
        dto.setPermissions(role.getPermissions());
        dto.setIsSystem(role.getIsSystem());
        dto.setCreatedAt(role.getCreatedAt());
        return dto;
    }
}
