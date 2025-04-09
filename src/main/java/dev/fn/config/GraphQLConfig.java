package dev.fn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import dev.fn.resolver.RoleGraphQLResolver;
import dev.fn.resolver.UserGraphQLResolver;

import org.springframework.core.io.ClassPathResource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.util.StreamUtils;

@Configuration
public class GraphQLConfig {

  private final UserGraphQLResolver userResolver;
  private final RoleGraphQLResolver roleResolver;

  public GraphQLConfig(UserGraphQLResolver userResolver, RoleGraphQLResolver roleResolver) {
    this.roleResolver = roleResolver;
    this.userResolver = userResolver;
  }

  @Bean
  public GraphQL graphQL() throws IOException {
    String sdl = StreamUtils.copyToString(
        new ClassPathResource("graphql/schema.graphqls").getInputStream(),
        StandardCharsets.UTF_8);

    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeRegistry = schemaParser.parse(sdl);

    RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
        .type("Query", builder -> builder
            .dataFetcher("getUser", environment -> userResolver.getUser(UUID.fromString(environment.getArgument("id"))))
            .dataFetcher("getRole", environment -> roleResolver.getRole(Long.parseLong(environment.getArgument("id"))))
            .dataFetcher("getAllUsers", environment -> userResolver.getAllUsers())
            .dataFetcher("getAllRoles", environment -> roleResolver.getAllRoles()))
        .type("Mutation", builder -> builder
            .dataFetcher("saveUser", environment -> userResolver.saveUser(environment.getArgument("input")))
            .dataFetcher("saveRole", environment -> roleResolver.saveRole(environment.getArgument("input")))
            .dataFetcher("updateUser", environment -> userResolver.updateUser(
                UUID.fromString(environment.getArgument("id")),
                environment.getArgument("input")))
            .dataFetcher("updateRole", environment -> roleResolver.updateRole(
                Long.parseLong(environment.getArgument("id")),
                environment.getArgument("input")))
            .dataFetcher("deleteUser",
                environment -> userResolver.deleteUser(UUID.fromString(environment.getArgument("id"))))
            .dataFetcher("deleteRole",
                environment -> roleResolver.deleteRole(Long.parseLong(environment.getArgument("id")))))
        .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);

    return GraphQL.newGraphQL(graphQLSchema).build();
  }
}
