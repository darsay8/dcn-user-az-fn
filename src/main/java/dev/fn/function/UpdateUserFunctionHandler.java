package dev.fn.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import dev.fn.model.UserDTO;
import dev.fn.service.UserService;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

@Component
public class UpdateUserFunctionHandler {

  private final UserService userService;
  private final ObjectMapper objectMapper;

  public UpdateUserFunctionHandler(UserService userService) {
    this.userService = userService;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @FunctionName("updateUserFunction")
  public HttpResponseMessage execute(
      @HttpTrigger(name = "request", methods = {
          HttpMethod.PUT }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<String> request,
      ExecutionContext context) {

    String userIdString = request.getQueryParameters().get("userId");
    String body = request.getBody();

    if (userIdString == null || body == null || body.isEmpty()) {
      return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
          .body("Missing userId or request body")
          .build();
    }

    try {
      UUID userId = UUID.fromString(userIdString);
      UserDTO userDTO = objectMapper.readValue(body, UserDTO.class);

      UserDTO updatedUser = userService.updateUser(userId, userDTO);

      return request.createResponseBuilder(HttpStatus.OK)
          .body(updatedUser)
          .header("Content-Type", "application/json")
          .build();
    } catch (IllegalArgumentException e) {
      return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
          .body("Invalid UUID format")
          .build();
    } catch (RuntimeException e) {
      return request.createResponseBuilder(HttpStatus.NOT_FOUND)
          .body(e.getMessage())
          .build();
    } catch (Exception e) {
      return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to parse user data: " + e.getMessage())
          .build();
    }
  }
}