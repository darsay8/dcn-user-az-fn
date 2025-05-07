
package dev.fn.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import dev.fn.model.UserDTO;
import dev.fn.service.UserService;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CreateUserFunctionHandler {

  @Autowired
  private UserService userService;

  @FunctionName("createUserFunction")
  public HttpResponseMessage execute(
      @HttpTrigger(name = "request", methods = {
          HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<UserDTO>> request,
      ExecutionContext context) {

    context.getLogger().info("CreateUserFunctionHandler started");
    context.getLogger().info("Request method: " + request.getHttpMethod());
    context.getLogger().info("Request URL: " + request.getUri().toString());
    context.getLogger().info("Request headers: " + request.getHeaders().toString());
    context.getLogger().info("Request body: " + request.getBody().toString());

    Optional<UserDTO> userDTOOptional = request.getBody();

    if (!userDTOOptional.isPresent()) {
      return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
          .body("User data is missing or invalid.")
          .header("Content-Type", "application/json")
          .build();
    }

    userDTOOptional.ifPresent(u -> context.getLogger().info("Received user: " + u.toString()));
    context.getLogger().info("Function name: " + context.getFunctionName());

    try {
      UserDTO savedUser = userService.saveUser(userDTOOptional.get());

      Map<String, Object> response = new HashMap<>();
      response.put("user", savedUser);
      response.put("roleStatus", "PENDING_ASSIGNMENT");
      response.put("message", "User created successfully. Role assignment in progress.");

      return request.createResponseBuilder(HttpStatus.CREATED)
          .body(response)
          .header("Content-Type", "application/json")
          .build();
    } catch (Exception e) {
      context.getLogger().severe("Error creating user: " + e.getMessage());
      return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error creating user: " + e.getMessage())
          .header("Content-Type", "application/json")
          .build();
    }
  }
}
