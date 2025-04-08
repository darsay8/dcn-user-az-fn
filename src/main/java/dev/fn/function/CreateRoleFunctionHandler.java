package dev.fn.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import dev.fn.model.RoleDTO;
import dev.fn.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Slf4j
@Component
public class CreateRoleFunctionHandler {

  private final RoleService roleService;

  public CreateRoleFunctionHandler(RoleService roleService) {
    this.roleService = roleService;
  }

  @FunctionName("createRoleFunction")
  public HttpResponseMessage execute(
      @HttpTrigger(name = "request", methods = {
          HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<RoleDTO>> request,
      ExecutionContext context) {

    context.getLogger().info("CreateRoleFunctionHandler started");

    Optional<RoleDTO> roleDTOOptional = request.getBody();

    if (roleDTOOptional.isPresent()) {
      RoleDTO savedRole = roleService.save(roleDTOOptional.get());
      return request.createResponseBuilder(HttpStatus.CREATED)
          .body(savedRole)
          .header("Content-Type", "application/json")
          .build();
    } else {
      return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
          .body("Role data is missing or invalid.")
          .build();
    }
  }
}