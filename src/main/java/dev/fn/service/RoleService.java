package dev.fn.service;

import org.springframework.stereotype.Service;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;

import dev.fn.model.Role;
import dev.fn.model.RoleDTO;
import dev.fn.model.User;
import dev.fn.repository.RoleRepository;
import dev.fn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

  private final UserRepository userRepository;
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

  public boolean delete(Long roleId) {
    Role roleToDelete = roleRepository.findById(roleId)
        .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

    Role defaultRole = getDefaultRole();

    if (roleToDelete.getRoleId().equals(defaultRole.getRoleId())) {
      throw new RuntimeException("Cannot delete the default role");
    }

    List<User> usersWithRole = userRepository.findByRole(roleToDelete);

    if (usersWithRole.isEmpty()) {
      roleRepository.delete(roleToDelete);
      log.info("Role {} deleted immediately because no users were assigned", roleId);
      return true;
    }

    int totalUsers = usersWithRole.size();
    for (User user : usersWithRole) {
      publishRoleReassignmentEvent(user.getUserId(), defaultRole.getRoleId(), roleId, totalUsers);
    }

    return false;
  }

  public void deleteAfterReassign(Long roleId) {
    Role role = roleRepository.findById(roleId)
        .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));

    // Optional safety check
    if (!userRepository.findByRole(role).isEmpty()) {
      throw new RuntimeException("Role still assigned to users");
    }

    roleRepository.deleteById(roleId);
    log.info("Force deleted role: {}", roleId);
  }

  private void publishRoleReassignmentEvent(UUID userId, Long newRoleId, Long roleToDelete, int totalUsers) {
    try {
      EventGridPublisherClient<EventGridEvent> client = new EventGridPublisherClientBuilder()
          .endpoint(System.getenv("TOPIC_ROLE_ENDPOINT"))
          .credential(new AzureKeyCredential(System.getenv("TOPIC_ROLE_KEY")))
          .buildEventGridEventPublisherClient();

      Map<String, Object> eventData = new HashMap<>();
      eventData.put("userId", userId.toString());
      eventData.put("newRoleId", newRoleId);
      eventData.put("roleToDelete", roleToDelete);
      eventData.put("totalUsers", totalUsers); // Add this!

      EventGridEvent event = new EventGridEvent(
          "app/roles/reassign",
          "App.Role.Reassignment",
          BinaryData.fromObject(eventData),
          "1.0");

      client.sendEvent(event);
    } catch (Exception e) {
      throw new RuntimeException("Failed to publish role reassignment event", e);
    }
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