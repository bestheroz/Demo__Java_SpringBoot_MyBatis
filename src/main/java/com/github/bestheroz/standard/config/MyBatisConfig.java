package com.github.bestheroz.standard.config;

import com.github.bestheroz.standard.common.enums.AuthorityEnum;
import com.github.bestheroz.standard.common.mybatis.handler.GenericListTypeHandler;
import java.util.List;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
@MapperScan("com.github.bestheroz")
public class MyBatisConfig {
  @Bean
  public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    sessionFactory.setDataSource(dataSource);

    // Try to load mapper XML files if they exist
    try {
      sessionFactory.setMapperLocations(
          new PathMatchingResourcePatternResolver()
              .getResources("classpath*:mybatis/mapper/**/*.xml"));
    } catch (Exception e) {
      // Mapper XMLs are optional, can use annotations instead
    }

    org.apache.ibatis.session.Configuration configuration =
        new org.apache.ibatis.session.Configuration();
    configuration.setMapUnderscoreToCamelCase(true);
    configuration
        .getTypeHandlerRegistry()
        .register(List.class, new GenericListTypeHandler<>(AuthorityEnum.class));

    sessionFactory.setConfiguration(configuration);
    return sessionFactory.getObject();
  }

  @Bean
  public ConfigurationCustomizer mybatisConfigurationCustomizer() {
    return configuration ->
        configuration
            .getTypeHandlerRegistry()
            .register(List.class, new GenericListTypeHandler<>(AuthorityEnum.class));
  }
}
