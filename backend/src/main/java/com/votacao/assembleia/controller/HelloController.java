package com.votacao.assembleia.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/v1")
@Tag(name = "Hello", description = "Health and smoke-test endpoint")
public class HelloController {

  private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

  @GetMapping(value = "/hello", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Hello World", description = "Returns a simple message from backend")
  @ApiResponses(value = {
    @ApiResponse(
      responseCode = "200",
      description = "Message returned",
      content = @Content(
        mediaType = "application/json",
        examples = @ExampleObject(value = "{\"message\":\"Hello World\"}")
      )
    )
  })
  public HelloResponse hello() {
    logger.info("GET /api/v1/hello - returning hello response");
    return new HelloResponse("Hello World");
  }

  public record HelloResponse(String message) {
  }
}
