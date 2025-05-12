package dev.fn.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;

import dev.fn.model.Role;
import dev.fn.model.RoleDTO;
import dev.fn.model.User;
import dev.fn.model.UserDTO;
import dev.fn.repository.UserRepository;
import dev.fn.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  private final RoleService roleService;

  public UserDTO saveUser(UserDTO userDTO) {
    String encryptedPassword = passwordEncoder.encode(userDTO.getPassword());

    var user = User.builder()
        .username(userDTO.getUsername())
        .email(userDTO.getEmail())
        .password(encryptedPassword)
        .build();

    User savedUser = userRepository.save(user);

    publishRoleAssignmentEvent(savedUser.getUserId());

    return toDTO(savedUser);
  }

  private void publishRoleAssignmentEvent(UUID userId) {
    log.info("Publishing role assignment event for user: {}", userId);

    try {
      EventGridPublisherClient<EventGridEvent> client = new EventGridPublisherClientBuilder()
          .endpoint(System.getenv("TOPIC_ROLE_ENDPOINT"))
          .credential(new AzureKeyCredential(System.getenv("TOPIC_ROLE_KEY")))
          .buildEventGridEventPublisherClient();

      Map<String, Object> eventData = new HashMap<>();
      eventData.put("userId", userId.toString());

      EventGridEvent event = new EventGridEvent(
          "app/roles/assign",
          "App.Role.Assignment",
          BinaryData.fromObject(eventData),
          "1.0");

      client.sendEvent(event);
      log.info("Role assignment event published successfully for user: {}", userId);
    } catch (Exception e) {
      log.error("Error publishing role assignment event: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to publish role assignment event", e);
    }
  }

  public UserDTO saveUserWithRoleAssigned(UserDTO userDTO) {
    String encryptedPassword = passwordEncoder.encode(userDTO.getPassword());

    Role role = userDTO.getRole() != null
        ? roleRepository.findById(userDTO.getRole().getRoleId())
            .orElseGet(roleService::getDefaultRole)
        : roleService.getDefaultRole();

    var user = User.builder()
        .username(userDTO.getUsername())
        .email(userDTO.getEmail())
        .password(encryptedPassword)
        .role(role)
        .build();

    User savedUser = userRepository.save(user);
    return toDTO(savedUser);
  }

  public UserDTO getUser(UUID id) {
    return userRepository.findById(id)
        .map(this::toDTO)
        .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
  }

  public List<UserDTO> getAllUsers() {
    return userRepository.findAll().stream()
        .map(this::toDTO)
        .toList();
  }

  public UserDTO updateUser(UUID id, UserDTO userDTO) {
    return userRepository.findById(id)
        .map(existingUser -> {

          if (userDTO.getUsername() != null) {
            existingUser.setUsername(userDTO.getUsername());
          }

          if (userDTO.getEmail() != null) {
            existingUser.setEmail(userDTO.getEmail());
          }

          if (userDTO.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
          }

          if (userDTO.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
          }

          if (userDTO.getRole() != null) {
            Role role = roleRepository.findById(userDTO.getRole().getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));
            existingUser.setRole(role);
          }

          return toDTO(userRepository.save(existingUser));
        })
        .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
  }

  public void deleteUser(UUID id) {
    if (!userRepository.existsById(id)) {
      throw new RuntimeException("User not found with id: " + id);
    }
    userRepository.deleteById(id);
  }

  public boolean verifyPassword(String rawPassword, String storedHashedPassword) {
    return passwordEncoder.matches(rawPassword, storedHashedPassword);
  }

  public void assignRole(UUID userId, Long roleId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    Role role = roleRepository.findById(roleId)
        .orElseThrow(() -> new RuntimeException("Role not found"));

    user.setRole(role);
    userRepository.save(user);
    log.info("Role {} assigned to user {}", roleId, userId);
  }

  private UserDTO toDTO(User user) {
    return UserDTO.builder()
        .userId(user.getUserId())
        .username(user.getUsername())
        .email(user.getEmail())
        .password(user.getPassword())
        .role(user.getRole() != null ? new RoleDTO(user.getRole().getRoleId(), user.getRole().getName()) : null)
        .build();
  }
}
