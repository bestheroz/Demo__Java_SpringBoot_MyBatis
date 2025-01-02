package com.github.bestheroz.standard.common.enums;

import io.github.bestheroz.mybatis.type.ValueEnum;
import lombok.Getter;

@Getter
public enum AuthorityEnum implements ValueEnum {
  ADMIN_VIEW("ADMIN_VIEW"),
  ADMIN_EDIT("ADMIN_EDIT"),
  USER_VIEW("USER_VIEW"),
  USER_EDIT("USER_EDIT"),
  NOTICE_VIEW("NOTICE_VIEW"),
  NOTICE_EDIT("NOTICE_EDIT");

  private final String value;

  AuthorityEnum(String value) {
    this.value = value;
  }
}
