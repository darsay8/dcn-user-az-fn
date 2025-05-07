package dev.fn.service;

import org.springframework.stereotype.Service;
import dev.fn.model.Role;
import dev.fn.model.RoleDTO;
import dev.fn.repository.RoleRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

  private final RoleRepository roleRepository;

  public RoleDTO save(RoleDTO roleDTO) {
    Role role = toEntity(roleDTO);
    Role savedRole = roleRepository.save(role);
    return toDTO(savedRole);
  }

  public RoleDTO findById(Long roleId) {
    return roleRepository.findById(roleId)
        .map(this::toDTO)
        .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));
  }

  public List<RoleDTO> findAll() {
    return roleRepository.findAll().stream()
        .map(this::toDTO)
        .collect(Collectors.toList());
  }

  public RoleDTO update(Long roleId, RoleDTO roleDTO) {
    return roleRepository.findById(roleId)
        .map(existingRole -> {
          existingRole.setName(roleDTO.getName());
          return toDTO(roleRepository.save(existingRole));
        })
        .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));
  }

  public void delete(Long id) {
    if (!roleRepository.existsById(id)) {
      throw new RuntimeException("Role not found with id: " + id);
    }
    roleRepository.deleteById(id);
  }

  public Role getDefaultRole() {
    return roleRepository.findByName("ROLE_USER")
        .orElseGet(() -> roleRepository.save(Role.builder()
            .name("ROLE_USER")
            .build()));
  }

  private RoleDTO toDTO(Role role) {
    return RoleDTO.builder()
        .roleId(role.getRoleId())
        .name(role.getName())
        .build();
  }

  private Role toEntity(RoleDTO dto) {
    Role role = new Role();
    role.setName(dto.getName());
    if (dto.getRoleId() != null) {
      role.setRoleId(dto.getRoleId());
    }
    return role;
  }
}