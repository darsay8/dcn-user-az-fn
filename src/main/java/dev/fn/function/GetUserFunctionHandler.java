package dev.fn.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import dev.fn.model.UserDTO;
import dev.fn.service.UserService;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GetUserFunctionHandler {

  @Autowired
  private UserService userService;

  @FunctionName("getUserFunction")
  public HttpResponseMessage execute(
      @HttpTrigger(name = "request", methods = {
          HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
      ExecutionContext context) {

    String userIdString = request.getQueryParameters().get("userId");

    context.getLogger().info("Received userId: " + userIdString);

    if (userIdString == null || userIdString.trim().isEmpty()) {
      return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
          .body("Missing userId parameter")
          .header("Content-Type", "application/json")
          .build();
    }

    try {
      UUID userId = UUID.fromString(userIdString);
      UserDTO userDTO = userService.getUser(userId);

      if (userDTO != null) {
        context.getLogger().info("=========>>>>> User found: " + userDTO.toString());
        return request.createResponseBuilder(HttpStatus.OK)
            .body(userDTO)
            .header("Content-Type", "application/json")
            .build();
      } else {
        return request.createResponseBuilder(HttpStatus.NOT_FOUND)
            .body("User with ID " + userId + " not found.")
            .header("Content-Type", "application/json")
            .build();
      }
    } catch (IllegalArgumentException e) {
      return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
          .body("Invalid userId format. Must be a valid UUID.")
          .header("Content-Type", "application/json")
          .build();
    }
  }
}
