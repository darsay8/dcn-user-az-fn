package dev.fn.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import dev.fn.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GetAllRolesFunctionHandler {

  private final RoleService roleService;

  public GetAllRolesFunctionHandler(RoleService roleService) {
    this.roleService = roleService;
  }

  @FunctionName("getAllRolesFunction")
  public HttpResponseMessage execute(
      @HttpTrigger(name = "request", methods = {
          HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Void> request,
      final ExecutionContext context) {

    context.getLogger().info("GetAllRolesFunctionHandler started");

    try {
      return request.createResponseBuilder(HttpStatus.OK)
          .body(roleService.findAll())
          .header("Content-Type", "application/json")
          .build();
    } catch (RuntimeException e) {
      return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error retrieving roles: " + e.getMessage())
          .build();
    }
  }
}