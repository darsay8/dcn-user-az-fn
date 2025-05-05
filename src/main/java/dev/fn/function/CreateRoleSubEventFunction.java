package dev.fn.function;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import dev.fn.model.RoleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import dev.fn.service.RoleService;

@Slf4j
@Component
public class CreateRoleSubEventFunction {

  private static final ObjectMapper mapper = new ObjectMapper();
  private final RoleService roleService;

  public CreateRoleSubEventFunction(RoleService roleService) {
    this.roleService = roleService;
  }

  @FunctionName("createRoleSubFunction")
  public void run(
      @EventGridTrigger(name = "eventGridEvent") String eventGridEventString,
      final ExecutionContext context) {

    context.getLogger().info("=========>>>>> Event received: " + eventGridEventString);

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
      RoleDTO roleDTO = mapper.convertValue(data, RoleDTO.class);

      context.getLogger().info("=========>>>>> Saving RoleDTO: " + roleDTO);
      roleService.save(roleDTO);

    } catch (Exception e) {
      context.getLogger().severe("=========>>>>> Error processing event: " + e.getMessage());
      e.printStackTrace();
    }

  }
}
