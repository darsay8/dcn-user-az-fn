package dev.fn.resolver;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Component;

import dev.fn.model.RoleDTO;
import dev.fn.service.RoleService;
import graphql.kickstart.tools.GraphQLMutationResolver;
import graphql.kickstart.tools.GraphQLQueryResolver;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoleGraphQLResolver implements GraphQLQueryResolver, GraphQLMutationResolver {

  private final RoleService roleService;

  @QueryMapping
  public RoleDTO getRole(@Argument Long id) {
    return roleService.findById(id);
  }

  @QueryMapping
  public List<RoleDTO> getAllRoles() {
    return roleService.findAll();
  }

  @MutationMapping
  public RoleDTO saveRole(@Argument("input") RoleDTO input) {
    return roleService.save(input);
  }

  @MutationMapping
  public RoleDTO updateRole(@Argument Long id, @Argument("input") RoleDTO input) {
    return roleService.update(id, input);
  }

  @MutationMapping
  public Boolean deleteRole(@Argument Long id) {
    roleService.delete(id);
    return true;
  }
}