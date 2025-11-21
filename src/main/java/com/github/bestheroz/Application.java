package com.github.bestheroz;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;

@OpenAPIDefinition(servers = {@Server(url = "/", description = "Default Server URL")})
@SpringBootApplication(
    exclude = {
      HibernateJpaAutoConfiguration.class,
      DataJpaRepositoriesAutoConfiguration.class,
      MybatisAutoConfiguration.class
    })
public class Application {

  public static void main(final String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
