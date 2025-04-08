package dev.fn.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
  private Long roleId;
  private String name;

}
