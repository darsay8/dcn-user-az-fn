package dev.fn.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import dev.fn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GetAllUsersFunctionHandler {

  @Autowired
  private UserService userService;

  @FunctionName("getAllUsersFunction")
  public HttpResponseMessage execute(
      @HttpTrigger(name = "request", methods = {
          HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Void> request,
      ExecutionContext context) {

    context.getLogger().info("Getting all users");
    return request.createResponseBuilder(HttpStatus.OK)
        .body(userService.getAllUsers())
        .header("Content-Type", "application/json")
        .build();
  }
}