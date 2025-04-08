package dev.fn.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.fn.model.Role;
import dev.fn.model.RoleDTO;
import dev.fn.model.User;
import dev.fn.model.UserDTO;
import dev.fn.repository.UserRepository;
import dev.fn.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  public UserDTO saveUser(UserDTO userDTO) {
    String encryptedPassword = passwordEncoder.encode(userDTO.getPassword());

    Role role = roleRepository.findById(userDTO.getRole().getRoleId())
        .orElseThrow(() -> new RuntimeException("Role not found"));

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

  private UserDTO toDTO(User user) {
    return new UserDTO(
        user.getUserId(),
        user.getUsername(),
        user.getEmail(),
        user.getPassword(),
        new RoleDTO(user.getRole().getRoleId(), user.getRole().getName()));
  }
}
