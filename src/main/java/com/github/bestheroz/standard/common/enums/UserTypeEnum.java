package com.github.bestheroz.standard.common.enums;

import io.github.bestheroz.mybatis.type.ValueEnum;
import lombok.Getter;

@Getter
public enum UserTypeEnum implements ValueEnum {
  ADMIN("ADMIN"),
  USER("USER");

  private final String value;

  UserTypeEnum(String value) {
    this.value = value;
  }
}
