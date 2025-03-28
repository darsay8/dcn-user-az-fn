package dev.fn.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import dev.fn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DeleteUserFunctionHandler {

  @Autowired
  private UserService userService;

  @FunctionName("deleteUserFunction")
  public HttpResponseMessage execute(
      @HttpTrigger(name = "request", methods = {
          HttpMethod.DELETE }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Void> request,
      ExecutionContext context) {

    String userIdString = request.getQueryParameters().get("userId");

    if (userIdString == null) {
      return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
          .body("Missing userId parameter")
          .build();
    }

    try {
      UUID userId = UUID.fromString(userIdString);
      userService.deleteUser(userId);
      return request.createResponseBuilder(HttpStatus.NO_CONTENT).build();
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