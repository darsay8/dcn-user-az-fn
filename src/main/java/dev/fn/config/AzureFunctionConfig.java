package dev.fn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.fn.model.RoleDTO;
import dev.fn.model.UserDTO;
import dev.fn.service.RoleService;
import dev.fn.service.UserService;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Configuration
public class AzureFunctionConfig {

  private UserService userService;
  private RoleService roleService;

  public AzureFunctionConfig(UserService userService, RoleService roleService) {
    this.userService = userService;
    this.roleService = roleService;
  }

  // Role Functions

  @Bean
  public Function<RoleDTO, RoleDTO> createRoleFunction() {
    return role -> roleService.save(role);
  }

  @Bean
  public Function<Long, RoleDTO> getRoleFunction() {
    return roleId -> roleService.findById(roleId);
  }

  @Bean
  public Supplier<List<RoleDTO>> getAllRolesFunction() {
    return () -> roleService.findAll();
  }

  @Bean
  public BiFunction<Long, RoleDTO, RoleDTO> updateRoleFunction() {
    return (id, role) -> roleService.update(id, role);
  }

  @Bean
  public Consumer<Long> deleteRoleFunction() {
    return id -> roleService.delete(id);
  }

  // User Functions

  @Bean
  public Function<UserDTO, UserDTO> createUserFunction() {
    return user -> userService.saveUser(user);
  }

  @Bean
  public Function<UUID, UserDTO> getUserFunction() {
    return userId -> userService.getUser(userId);
  }

  @Bean
  public Supplier<List<UserDTO>> getAllUsersFunction() {
    return () -> userService.getAllUsers();
  }

  @Bean
  public Function<UUID, UserDTO> getAllUsersFunctionGraphQL() {
    return userId -> userService.getUser(userId);
  }

  @Bean
  public BiFunction<UUID, UserDTO, UserDTO> updateUserFunction() {
    return (id, user) -> userService.updateUser(id, user);
  }

  @Bean
  public Consumer<UUID> deleteUserFunction() {
    return id -> userService.deleteUser(id);
  }
}