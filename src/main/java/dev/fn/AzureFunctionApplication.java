package dev.fn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication(scanBasePackages = "dev.fn")
public class AzureFunctionApplication {

  public static void main(String[] args) {
    SpringApplication.run(AzureFunctionApplication.class, args);
  }
}