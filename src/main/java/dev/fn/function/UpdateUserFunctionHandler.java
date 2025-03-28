package dev.fn.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import dev.fn.model.UserDTO;
import dev.fn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UpdateUserFunctionHandler {

  @Autowired
  private UserService userService;

  @FunctionName("updateUserFunction")
  public HttpResponseMessage execute(
      @HttpTrigger(name = "request", methods = {
          HttpMethod.PUT }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<UserDTO>> request,
      ExecutionContext context) {

    String userIdString = request.getQueryParameters().get("userId");
    Optional<UserDTO> userDTOOptional = request.getBody();

    if (userIdString == null || userDTOOptional.isEmpty()) {
      return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
          .body("Missing userId or user data")
          .build();
    }

    try {
      UUID userId = UUID.fromString(userIdString);
      UserDTO updatedUser = userService.updateUser(userId, userDTOOptional.get());
      return request.createResponseBuilder(HttpStatus.OK)
          .body(updatedUser)
          .build();
    } catch (IllegalArgumentException e) {
      return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
          .body("Invalid UUID format")
          .build();
    } catch (RuntimeException e) {
      return request.createResponseBuilder(HttpStatus.NOT_FOUND)
          .body(e.getMessage())
          .build();
    }
  }
}