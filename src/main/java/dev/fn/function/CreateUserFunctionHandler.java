package dev.fn.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import dev.fn.model.UserDTO;
import dev.fn.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateUserFunctionHandler {

  private final UserService userService;

  @FunctionName("createUserFunction")
  public HttpResponseMessage execute(
      @HttpTrigger(name = "request", methods = {
          HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<UserDTO>> request,
      ExecutionContext context) {

    context.getLogger().info("CreateUserFunctionHandler started");

    Optional<UserDTO> userDTOOptional = request.getBody();

    if (!userDTOOptional.isPresent()) {
      return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
          .body("User data is missing or invalid.")
          .build();
    }

    try {
      UserDTO userDTO = userDTOOptional.get();
      Map<String, Object> response = new HashMap<>();

      if (userDTO.getRole() != null) {
        // Create user with specified role
        UserDTO savedUser = userService.saveUserWithRoleAssigned(userDTO);
        response.put("user", savedUser);
        response.put("roleStatus", "ROLE_ASSIGNED");
        response.put("message", "User created successfully with specified role.");
      } else {
        // Create user without role and trigger event
        UserDTO savedUser = userService.saveUser(userDTO);
        response.put("user", savedUser);
        response.put("roleStatus", "PENDING_ASSIGNMENT");
        response.put("message", "User created successfully. Default role assignment in progress.");
      }

      return request.createResponseBuilder(HttpStatus.CREATED)
          .body(response)
          .header("Content-Type", "application/json")
          .build();

    } catch (Exception e) {
      context.getLogger().severe("Error creating user: " + e.getMessage());
      return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error creating user: " + e.getMessage())
          .build();
    }
  }
}
