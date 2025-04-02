package com.huybq.fund_management.domain.role;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Optional<Role> getRoleById(Integer id) {
        return roleRepository.findById(id);
    }

    public Optional<Role> getRoleByName(String name) {
        return roleRepository.findByName(name);
    }

    public Role createRole(RoleDTO dto) {
        var role = Role.builder()
                .name(dto.name().toUpperCase())
                .build();
        return roleRepository.save(role);
    }

    public Role updateRole(Integer id, RoleDTO dto) {
        return roleRepository.findById(id)
                .map(existingRole -> {
                    existingRole.setName(dto.name().toUpperCase());
                    return roleRepository.save(existingRole);
                })
                .orElseThrow(() -> new RuntimeException("Role not found"));
    }

    public void deleteRole(Integer id) {
        roleRepository.deleteById(id);
    }
}
