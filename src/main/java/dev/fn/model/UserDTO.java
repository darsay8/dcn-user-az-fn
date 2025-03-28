package dev.fn.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserDTO {
  private UUID userId;
  private String username;
  private String email;
  private String password;
  private Role role;
}
