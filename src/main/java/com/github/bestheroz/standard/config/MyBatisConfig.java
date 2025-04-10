package com.github.bestheroz.standard.config;

import com.github.bestheroz.standard.common.enums.AuthorityEnum;
import com.github.bestheroz.standard.common.mybatis.handler.GenericListTypeHandler;
import java.util.List;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.github.bestheroz")
public class MyBatisConfig {
  @Bean
  public ConfigurationCustomizer mybatisConfigurationCustomizer() {
    return configuration ->
        configuration
            .getTypeHandlerRegistry()
            .register(List.class, new GenericListTypeHandler<>(AuthorityEnum.class));
  }
}
