package dev.fn.model;

import java.util.UUID;

import lombok.*;

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
  private RoleDTO role;
}
