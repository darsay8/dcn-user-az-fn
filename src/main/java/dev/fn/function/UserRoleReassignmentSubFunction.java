package dev.fn.function;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import dev.fn.service.UserService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class UserRoleReassignmentSubFunction {

  private static final ObjectMapper mapper = new ObjectMapper();
  private final UserService userService;

  // In-memory map: roleId → counter
  private final Map<Long, AtomicInteger> reassignmentCounters = new ConcurrentHashMap<>();

  public UserRoleReassignmentSubFunction(UserService userService) {
    this.userService = userService;
  }

  @FunctionName("handleUserRoleReassignment")
  public void run(
      @EventGridTrigger(name = "eventGridEvent") String eventGridEventString,
      final ExecutionContext context) {

    context.getLogger().info("=========>>>>> Processing role reassignment event");

    try {
      JsonNode eventGridEvent = mapper.readTree(eventGridEventString);
      JsonNode data = eventGridEvent.path("data");

      UUID userId = UUID.fromString(data.path("userId").asText());
      Long newRoleId = data.path("newRoleId").asLong();
      Long roleToDelete = data.path("roleToDelete").asLong();
      int totalUsers = data.path("totalUsers").asInt();

      context.getLogger().info("Reassigning user " + userId + " to role " + newRoleId);
      userService.assignRole(userId, newRoleId);
      context.getLogger().info("User role reassigned successfully");

      // Track reassignment completion
      int count = reassignmentCounters
          .computeIfAbsent(roleToDelete, k -> new AtomicInteger(totalUsers))
          .decrementAndGet();

      if (count == 0) {
        context.getLogger().info("All users reassigned. Publishing role deletion event.");
        publishRoleDeletionEvent(roleToDelete);
        reassignmentCounters.remove(roleToDelete);
      }

    } catch (Exception e) {
      context.getLogger().severe("Error processing role reassignment: " + e.getMessage());
      throw new RuntimeException("Failed to process role reassignment event", e);
    }
  }

  private void publishRoleDeletionEvent(Long roleId) {
    try {
      EventGridPublisherClient<EventGridEvent> client = new EventGridPublisherClientBuilder()
          .endpoint(System.getenv("TOPIC_ROLE_ENDPOINT"))
          .credential(new AzureKeyCredential(System.getenv("TOPIC_ROLE_KEY")))
          .buildEventGridEventPublisherClient();

      EventGridEvent event = new EventGridEvent(
          "app/roles/delete",
          "App.Role.Deletion",
          BinaryData.fromObject(Map.of("roleId", roleId)),
          "1.0");

      client.sendEvent(event);
    } catch (Exception e) {
      throw new RuntimeException("Failed to publish role deletion event", e);
    }
  }
}