package com.gearup.userauth.service;

import com.gearup.userauth.dto.RoleResponse;
import com.gearup.userauth.exception.ResourceNotFoundException;
import com.gearup.userauth.model.Role;
import com.gearup.userauth.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleService {

    // Logger retained for potential future diagnostic use; suppress unused warning.
    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(RoleResponse::fromRole)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleResponseByName(String name) {
        Role role = getRoleByName(name);
        return RoleResponse.fromRole(role);
    }

    @Transactional(readOnly = true)
    public Role getRoleByName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResourceNotFoundException("Role", "name", "<blank>");
        }
        String normalized = name.trim();
        // Try exact (with permissions) first (already case-insensitive via LOWER in query), fallback to ignore case basic lookup
        return roleRepository.findByNameWithPermissions(normalized)
                .or(() -> roleRepository.findByNameIgnoreCase(normalized))
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", normalized));
    }
}
