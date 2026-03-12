package com.votacao.assembleia;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures CORS for backend API endpoints.
 *
 * <p>This allows the frontend running on {@code http://localhost:3000} to call
 * endpoints under {@code /api/**} during local development. It permits common
 * HTTP methods used by the application and accepts any request header.
 */
@Configuration
public class CorsConfig {

  /**
   * Registers CORS rules in Spring MVC.
   *
   * <p>Scope: applies only to routes that start with {@code /api/}.
   */
  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/**")
          .allowedOrigins("http://localhost:3000")
          .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
          .allowedHeaders("*");
      }
    };
  }
}
