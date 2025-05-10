package dev.fn.function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import dev.fn.service.RoleService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserRoleDeletionSubFunction {

  private static final ObjectMapper mapper = new ObjectMapper();
  private final RoleService roleService;

  public UserRoleDeletionSubFunction(RoleService roleService) {
    this.roleService = roleService;
  }

  @FunctionName("handleUserRoleDeletion")
  public void run(
      @EventGridTrigger(name = "eventGridEvent") String eventGridEventString,
      final ExecutionContext context) {

    context.getLogger().info("=========>>>>> Processing role deletion event");

    try {
      JsonNode eventGridEvent = mapper.readTree(eventGridEventString);
      String eventType = eventGridEvent.path("eventType").asText();

      if (!"App.Role.Deletion".equals(eventType)) {
        context.getLogger().info("Ignoring event of type: " + eventType);
        return;
      }

      JsonNode data = eventGridEvent.path("data");
      if (data.isMissingNode()) {
        throw new IllegalArgumentException("Missing 'data' field in event");
      }

      Long roleId = data.path("roleId").asLong();
      context.getLogger().info("Deleting role with id: " + roleId);

      // Perform the actual deletion
      roleService.deleteAfterReassign(roleId);

      context.getLogger().info("Role deleted successfully: " + roleId);

    } catch (Exception e) {
      context.getLogger().severe("Error processing role deletion event: " + e.getMessage());
      throw new RuntimeException("Failed to process role deletion event", e);
    }
  }
}