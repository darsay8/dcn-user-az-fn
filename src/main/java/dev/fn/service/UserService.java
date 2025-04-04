package dev.fn.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.fn.model.User;
import dev.fn.model.UserDTO;
import dev.fn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserDTO saveUser(UserDTO userDTO) {
    String encryptedPassword = passwordEncoder.encode(userDTO.getPassword());

    var user = User.builder()
        .username(userDTO.getUsername())
        .email(userDTO.getEmail())
        .password(encryptedPassword)
        .role(userDTO.getRole())
        .build();

    User savedUser = userRepository.save(user);

    return new UserDTO(savedUser.getUserId(), savedUser.getUsername(), savedUser.getEmail(), savedUser.getPassword(),
        savedUser.getRole());

  }

  public UserDTO getUser(UUID id) {
    var user = userRepository.findById(id).orElse(new User());

    return new UserDTO(user.getUserId(),
        user.getUsername(), user.getEmail(), user.getPassword(), user.getRole());
  }

  public List<UserDTO> getAllUsers() {
    List<User> users = userRepository.findAll();
    return users.stream()
        .map(user -> new UserDTO(user.getUserId(), user.getUsername(), user.getEmail(), user.getPassword(),
            user.getRole()))
        .toList();
  }

  public UserDTO updateUser(UUID id, UserDTO userDTO) {
    return userRepository.findById(id)
        .map(existingUser -> {
          existingUser.setUsername(userDTO.getUsername());
          existingUser.setEmail(userDTO.getEmail());
          if (userDTO.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
          }
          existingUser.setRole(userDTO.getRole());
          return new UserDTO(
              userRepository.save(existingUser).getUserId(),
              existingUser.getUsername(),
              existingUser.getEmail(),
              existingUser.getPassword(),
              existingUser.getRole());
        })
        .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
  }

  public void deleteUser(UUID id) {
    if (userRepository.existsById(id)) {
      userRepository.deleteById(id);
    } else {
      throw new RuntimeException("User not found with id: " + id);
    }
  }

  public boolean verifyPassword(String rawPassword, String storedHashedPassword) {
    return passwordEncoder.matches(rawPassword, storedHashedPassword);
  }
}
