package dev.fn.resolver;

import java.util.List;
import java.util.UUID;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Component;

import dev.fn.model.UserDTO;
import dev.fn.service.UserService;
import graphql.kickstart.tools.GraphQLMutationResolver;
import graphql.kickstart.tools.GraphQLQueryResolver;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserGraphQLResolver implements GraphQLQueryResolver, GraphQLMutationResolver {
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
  public UserDTO saveUser(@Argument("input") UserDTO input) {
    return userService.saveUser(input);
  }

  @MutationMapping
  public UserDTO updateUser(@Argument UUID id, @Argument("input") UserDTO input) {
    return userService.updateUser(id, input);
  }

  @MutationMapping
  public Boolean deleteUser(@Argument UUID id) {
    userService.deleteUser(id);
    return true;
  }
}
