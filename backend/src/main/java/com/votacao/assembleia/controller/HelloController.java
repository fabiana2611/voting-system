package com.votacao.assembleia.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/v1")
public class HelloController {

  @GetMapping(value = "/hello", produces = MediaType.APPLICATION_JSON_VALUE)
  public HelloResponse hello() {
    return new HelloResponse("Hello World");
  }

  public record HelloResponse(String message) {
  }
}
