package com.votacao.assembleia.repository;

import java.util.List;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("null")
@Configuration
public class UserSeeder {

  @Bean
  ApplicationRunner seedUsers(UserRepository userRepository) {
    return args -> {
      List<UserEntity> users = List.of(
        new UserEntity("678.990.942-75", "Ana"),
        new UserEntity("441.373.971-04", "Bruno"),
        new UserEntity("813.663.550-16", "Carla"),
        new UserEntity("912.301.772-48", "Diego"),
        new UserEntity("369.651.256-75", "Elisa")
      );

      for (UserEntity user : users) {
        if (!userRepository.existsById(user.getId())) {
          userRepository.save(user);
        }
      }
    };
  }
}
