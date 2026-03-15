package com.github.bestheroz.standard.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.util.StdDateFormat;

@Configuration
public class MapperConfig {
  @Bean
  public JsonMapperBuilderCustomizer jacksonCustomizer() {
    return builder -> builder.defaultDateFormat(new StdDateFormat().withColonInTimeZone(true));
  }
}
