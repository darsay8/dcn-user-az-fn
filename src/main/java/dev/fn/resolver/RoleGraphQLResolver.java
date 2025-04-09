package dev.fn.resolver;

import java.util.List;
import java.util.Map;

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
  public RoleDTO saveRole(@Argument("input") Map<String, Object> input) {
    return roleService.save(mapToRoleDTO(input));
  }

  @MutationMapping
  public RoleDTO updateRole(@Argument Long id, @Argument("input") Map<String, Object> input) {
    return roleService.update(id, mapToRoleDTO(input));
  }

  @MutationMapping
  public Boolean deleteRole(@Argument Long id) {
    roleService.delete(id);
    return true;
  }

  private RoleDTO mapToRoleDTO(Map<String, Object> input) {
    if (input == null) {
      return null;
    }
    RoleDTO roleDTO = new RoleDTO();
    roleDTO.setName((String) input.get("name"));
    return roleDTO;
  }
}