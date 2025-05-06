package dev.fn.function;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import dev.fn.model.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import dev.fn.service.UserService;

@Slf4j
@Component
public class CreateUserSubEventFunction {

  private static final ObjectMapper mapper = new ObjectMapper();
  private final UserService userService;

  public CreateUserSubEventFunction(UserService userService) {
    this.userService = userService;
  }

  @FunctionName("createUserSubFunction")
  public void run(
      @EventGridTrigger(name = "eventGridEvent") String eventGridEventString,
      final ExecutionContext context) {

    context.getLogger().info("=========>>>>> User Event received: " + eventGridEventString);

    try {
      Map<String, Object> eventMap = mapper.readValue(eventGridEventString, new TypeReference<Map<String, Object>>() {
      });

      context.getLogger().info("=========>>>>> Event map: " + eventMap);

      String eventType = (String) eventMap.get("eventType");

      if ("Microsoft.EventGrid.SubscriptionValidationEvent".equals(eventType)) {
        context.getLogger().info("=========>>>>> Handling subscription validation event.");
        return;
      }

      Object data = eventMap.get("data");
      UserDTO userDTO = mapper.convertValue(data, UserDTO.class);

      context.getLogger().info("=========>>>>> Saving UserDTO: " + userDTO);
      userService.saveUser(userDTO);

    } catch (Exception e) {
      context.getLogger().severe("=========>>>>> Error processing user event: " + e.getMessage());
      e.printStackTrace();
    }
  }
}