package dev.fn.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import dev.fn.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeleteRoleFunctionHandler {

  private final RoleService roleService;

  public DeleteRoleFunctionHandler(RoleService roleService) {
    this.roleService = roleService;
  }

  @FunctionName("deleteRoleFunction")
  public HttpResponseMessage execute(
      @HttpTrigger(name = "request", methods = {
          HttpMethod.DELETE }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Void> request,
      ExecutionContext context) {

    String roleIdString = request.getQueryParameters().get("roleId");

    if (roleIdString == null) {
      return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
          .body("Missing roleId parameter")
          .build();
    }

    try {
      Long roleId = Long.parseLong(roleIdString);
      roleService.delete(roleId);
      return request.createResponseBuilder(HttpStatus.NO_CONTENT).build();
    } catch (IllegalArgumentException e) {
      return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
          .body("Invalid roleId format")
          .build();
    } catch (RuntimeException e) {
      return request.createResponseBuilder(HttpStatus.NOT_FOUND)
          .body(e.getMessage())
          .build();
    }
  }
}