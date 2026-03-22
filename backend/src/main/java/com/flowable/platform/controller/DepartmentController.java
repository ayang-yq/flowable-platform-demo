package com.flowable.platform.controller;

import com.flowable.platform.dto.ApiResponse;
import com.flowable.platform.dto.DepartmentDTO;
import com.flowable.platform.dto.CreateDepartmentRequest;
import com.flowable.platform.entity.Department;
import com.flowable.platform.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> listDepartments() {
        List<DepartmentDTO> departments = departmentService.getRootDepartments().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentDTO>> getDepartment(@PathVariable UUID id) {
        Department dept = departmentService.getDepartmentByIdOrThrow(id);
        return ResponseEntity.ok(ApiResponse.success(toDTO(dept)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentDTO>> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request) {
        Department dept = departmentService.createDepartment(
                request.getName(), request.getCode(), request.getParentId(), request.getManagerId());
        return ResponseEntity.ok(ApiResponse.success(toDTO(dept)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentDTO>> updateDepartment(
            @PathVariable UUID id, @RequestBody Map<String, Object> updates) {
        String name = (String) updates.get("name");
        UUID managerId = updates.get("managerId") != null ? UUID.fromString(updates.get("managerId").toString()) : null;
        Department dept = departmentService.updateDepartment(id, name, managerId);
        return ResponseEntity.ok(ApiResponse.success(toDTO(dept)));
    }

    @PutMapping("/{id}/users")
    public ResponseEntity<ApiResponse<Void>> updateDepartmentUsers(
            @PathVariable UUID id, @RequestBody Map<String, Object> body) {
        String action = (String) body.get("action");
        @SuppressWarnings("unchecked")
        List<String> userIdStrings = (List<String>) body.get("userIds");
        Set<UUID> userIds = userIdStrings.stream().map(UUID::fromString).collect(Collectors.toSet());

        if ("add".equals(action)) {
            departmentService.addUsersToDepartment(id, userIds);
        } else if ("remove".equals(action)) {
            departmentService.removeUsersFromDepartment(id, userIds);
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("INVALID_ACTION", "Action must be 'add' or 'remove'"));
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private DepartmentDTO toDTO(Department dept) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(dept.getId());
        dto.setName(dept.getName());
        dto.setCode(dept.getCode());
        dto.setPath(dept.getPath());
        dto.setLevel(dept.getLevel());
        dto.setIsActive(dept.getIsActive());
        dto.setCreatedAt(dept.getCreatedAt());
        if (dept.getParent() != null) {
            dto.setParentId(dept.getParent().getId());
            dto.setParentName(dept.getParent().getName());
        }
        if (dept.getManager() != null) {
            dto.setManagerId(dept.getManager().getId());
            dto.setManagerName(dept.getManager().getDisplayName());
        }
        if (dept.getChildren() != null && !dept.getChildren().isEmpty()) {
            dto.setChildren(dept.getChildren().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
