package dev.fn.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import dev.fn.model.RoleDTO;
import dev.fn.service.RoleService;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GetRoleFunctionHandler {

  private final RoleService roleService;

  public GetRoleFunctionHandler(@Autowired RoleService roleService) {
    this.roleService = roleService;
  }

  @FunctionName("getRoleFunction")
  public HttpResponseMessage execute(
      @HttpTrigger(name = "request", methods = {
          HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
      ExecutionContext context) {

    String roleIdString = request.getQueryParameters().get("roleId");
    context.getLogger().info("Received roleId: " + roleIdString);

    if (roleIdString == null || roleIdString.trim().isEmpty()) {
      return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
          .body("Missing roleId parameter")
          .header("Content-Type", "application/json")
          .build();
    }

    try {
      Long roleId = Long.parseLong(roleIdString);
      RoleDTO roleDTO = roleService.findById(roleId);

      if (roleDTO != null) {
        context.getLogger().info("=========>>>>> Role found: " + roleDTO.toString());
        return request.createResponseBuilder(HttpStatus.OK)
            .body(roleDTO)
            .header("Content-Type", "application/json")
            .build();

      } else {
        return request.createResponseBuilder(HttpStatus.NOT_FOUND)
            .body("Role ID " + roleId + " not found.")
            .header("Content-Type", "application/json")
            .build();

      }

    } catch (IllegalArgumentException e) {
      return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
          .body("Invalid roleId format. Must be a valid Long.")
          .header("Content-Type", "application/json")
          .build();
    }
  }
}