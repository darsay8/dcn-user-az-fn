
package dev.fn.function;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import dev.fn.model.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Slf4j
@Component
public class CreateUserPubEventFunction {
  @FunctionName("createUserPubFunction")
  public HttpResponseMessage run(
      @HttpTrigger(name = "req", methods = {
          HttpMethod.POST }, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<UserDTO>> request,
      final ExecutionContext context) {

    context.getLogger().info("=========>>>>> Starting publishUserEvent");

    Optional<UserDTO> userDTOOptional = request.getBody();

    if (!userDTOOptional.isPresent()) {
      return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
          .body("User data is missing or invalid.")
          .build();
    }

    try {
      EventGridPublisherClient<EventGridEvent> client = new EventGridPublisherClientBuilder()
          .endpoint(System.getenv("TOPIC_USER_ENDPOINT"))
          .credential(new AzureKeyCredential(System.getenv("TOPIC_USER_KEY")))
          .buildEventGridEventPublisherClient();

      UserDTO userDTO = userDTOOptional.get();

      EventGridEvent event = new EventGridEvent(
          "app/users/create",
          "App.User.Created",
          BinaryData.fromObject(userDTO),
          "1.0");

      context.getLogger().info("=========>>>>> Publishing event: " + event);
      client.sendEvent(event);

      return request.createResponseBuilder(HttpStatus.ACCEPTED)
          .header("Content-Type", "application/json")
          .body("User event published successfully.")
          .build();

    } catch (Exception e) {
      context.getLogger().severe("=========>>>>> Error sending event: " + e.getMessage());
      return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to publish event::::" + e.getMessage())
          .build();
    }
  }
}
