
package dev.fn.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import dev.fn.model.UserDTO;
import dev.fn.service.UserService;
import lombok.extern.slf4j.Slf4j;

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

    if (userDTOOptional.isPresent()) {
      context.getLogger().info("User data is present in the request body.");
    } else {
      context.getLogger().warning("User data is missing in the request body.");
    }

    userDTOOptional.ifPresent(u -> context.getLogger().info("Received user: " + u.toString()));
    context.getLogger().info("Function name: " + context.getFunctionName());

    if (userDTOOptional.isPresent()) {
      UserDTO savedUser = userService.saveUser(userDTOOptional.get());

      return request.createResponseBuilder(HttpStatus.CREATED)
          .body(savedUser)
          .header("Content-Type", "application/json")
          .build();
    } else {
      return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
          .body("User data is missing or invalid.")
          .header("Content-Type", "application/json")
          .build();
    }
  }
}
