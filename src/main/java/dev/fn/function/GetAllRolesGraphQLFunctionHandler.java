package dev.fn.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import graphql.ExecutionInput;
import graphql.GraphQL;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GetAllRolesGraphQLFunctionHandler {

  private GraphQL graphQL;

  public GetAllRolesGraphQLFunctionHandler(GraphQL graphQL) {
    this.graphQL = graphQL;
  }

  @FunctionName("getAllRolesFunctionGraphQL")
  public HttpResponseMessage execute(
      @HttpTrigger(name = "req", methods = {
          HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Map<String, Object>> request,
      final ExecutionContext context) {

    String query = (String) request.getBody().get("query");

    context.getLogger().info("GraphQL query: " + query);

    ExecutionInput executionInput = ExecutionInput.newExecutionInput()
        .query(query)
        .build();

    Map<String, Object> result = graphQL.execute(executionInput).toSpecification();

    return request.createResponseBuilder(HttpStatus.OK)
        .body(result)
        .header("Content-Type", "application/json")
        .build();

  }
}
