package dev.fn.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import dev.fn.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteRoleFunctionHandler {

  private final RoleService roleService;

  @FunctionName("deleteRoleFunction")
  public HttpResponseMessage execute(
      @HttpTrigger(name = "request", methods = {
          HttpMethod.DELETE }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Void> request,
      ExecutionContext context) {

    String roleIdStr = request.getQueryParameters().get("roleId");

    try {
      if (roleIdStr == null || roleIdStr.isBlank()) {
        throw new IllegalArgumentException("Missing required query parameter: roleId");
      }

      Long roleId = Long.parseLong(roleIdStr);
      context.getLogger().info("Deleting role with ID: {}" + roleId);

      boolean deletedImmediately = roleService.delete(roleId);

      Map<String, String> response = new HashMap<>();
      response.put("roleId", roleIdStr);
      response.put("status", "SUCCESS");

      if (deletedImmediately) {
        response.put("message", "Role deleted immediately. No users required reassignment.");
        return request.createResponseBuilder(HttpStatus.NO_CONTENT)
            .header("Content-Type", "application/json")
            .body(response)
            .build();
      } else {
        response.put("message", "Role deleted successfully after reassigning users.");
        return request.createResponseBuilder(HttpStatus.OK)
            .header("Content-Type", "application/json")
            .body(response)
            .build();
      }

    } catch (RuntimeException e) {
      log.error("Error during role deletion: {}", e.getMessage(), e);
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("message", e.getMessage());
      errorResponse.put("roleId", roleIdStr != null ? roleIdStr : "unknown");
      errorResponse.put("status", "ERROR");

      return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
          .header("Content-Type", "application/json")
          .body(errorResponse)
          .build();
    }
  }
}