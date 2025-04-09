package dev.fn.resolver;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Component;

import dev.fn.model.UserDTO;
import dev.fn.service.RoleService;
import dev.fn.service.UserService;
import graphql.kickstart.tools.GraphQLMutationResolver;
import graphql.kickstart.tools.GraphQLQueryResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserGraphQLResolver implements GraphQLQueryResolver, GraphQLMutationResolver {

  private final RoleService roleService;
  private final UserService userService;

  @QueryMapping
  public UserDTO getUser(@Argument UUID id) {
    return userService.getUser(id);
  }

  @QueryMapping
  public List<UserDTO> getAllUsers() {
    return userService.getAllUsers();
  }

  @MutationMapping
  public UserDTO saveUser(@Argument("input") Map<String, Object> input) {
    return userService.saveUser(mapToUserDTO(input));
  }

  @MutationMapping
  public UserDTO updateUser(@Argument UUID id, @Argument("input") Map<String, Object> input) {

    return userService.updateUser(id, mapToUserDTO(input));
  }

  @MutationMapping
  public Boolean deleteUser(@Argument UUID id) {
    userService.deleteUser(id);
    return true;
  }

  private UserDTO mapToUserDTO(Map<String, Object> input) {
    if (input == null) {
      return null;
    }
    UserDTO userDTO = new UserDTO();
    userDTO.setUsername((String) input.get("username"));
    userDTO.setEmail((String) input.get("email"));
    userDTO.setPassword((String) input.get("password"));

    Object roleIdObj = input.get("roleId");
    if (roleIdObj != null) {
      Long roleId;
      if (roleIdObj instanceof Number) {
        roleId = ((Number) roleIdObj).longValue();
      } else {
        roleId = Long.parseLong(roleIdObj.toString());
      }
      userDTO.setRole(roleService.findById(roleId));
    }

    return userDTO;
  }
}
