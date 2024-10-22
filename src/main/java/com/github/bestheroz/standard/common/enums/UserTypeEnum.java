package com.github.bestheroz.standard.common.enums;

import lombok.Getter;

@Getter
public enum UserTypeEnum {
  ADMIN("admin"),
  USER("user");

  private final String value;

  UserTypeEnum(String value) {
    this.value = value;
  }
}
