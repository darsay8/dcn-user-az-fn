package dev.fn.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import dev.fn.model.RoleDTO;
import dev.fn.service.RoleService;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UpdateRoleFunctionHandler {

  private final RoleService roleService;

  public UpdateRoleFunctionHandler(RoleService roleService) {
    this.roleService = roleService;
  }

  @FunctionName("updateRoleFunction")
  public HttpResponseMessage execute(
      @HttpTrigger(name = "request", methods = {
          HttpMethod.PUT }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<RoleDTO>> request,
      ExecutionContext context) {

    String roleIdString = request.getQueryParameters().get("roleId");
    Optional<RoleDTO> roleDTOOptional = request.getBody();

    if (roleIdString == null || roleDTOOptional.isEmpty()) {
      return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
          .body("Missing roleId or role data")
          .build();
    }

    try {
      Long roleId = Long.parseLong(roleIdString);
      RoleDTO updatedRole = roleService.update(roleId, roleDTOOptional.get());
      return request.createResponseBuilder(HttpStatus.OK)
          .body(updatedRole)
          .header("Content-Type", "application/json")
          .build();
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