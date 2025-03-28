package dev.fn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.fn.model.UserDTO;
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

  public AzureFunctionConfig(UserService userService) {
    this.userService = userService;
  }

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
  public BiFunction<UUID, UserDTO, UserDTO> updateUserFunction() {
    return (id, user) -> userService.updateUser(id, user);
  }

  @Bean
  public Consumer<UUID> deleteUserFunction() {
    return id -> userService.deleteUser(id);
  }
}