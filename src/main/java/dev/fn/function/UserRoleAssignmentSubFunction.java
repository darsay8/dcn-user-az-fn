package dev.fn.function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import dev.fn.service.UserService;
import dev.fn.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class UserRoleAssignmentSubFunction {

  private final UserService userService;
  private final RoleService roleService;

  public UserRoleAssignmentSubFunction(UserService userService, RoleService roleService) {
    this.userService = userService;
    this.roleService = roleService;
  }

  @FunctionName("handleUserRoleAssignment")
  public void run(
      @EventGridTrigger(name = "roleAssignment") String eventData,
      final ExecutionContext context) {

    context.getLogger().info("=========>>>>> Processing role assignment for user: " + eventData);

    try {
      var defaultRole = roleService.getDefaultRole();

      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(eventData);
      String extractedUserId = root.path("data").asText();
      // String extractedUserId = root.path("data").path("userId").asText();

      userService.assignRole(UUID.fromString(extractedUserId), defaultRole.getRoleId());

      context.getLogger().info("=========>>>>> Default role assigned successfully to user: " + extractedUserId);
    } catch (Exception e) {
      context.getLogger().severe("=========>>>>> Error assigning role to user: " + e.getMessage());
      throw new RuntimeException("Failed to assign role to user", e);
    }
  }
}