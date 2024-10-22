package com.github.bestheroz.demo.entity.handler;

import com.github.bestheroz.standard.common.enums.AuthorityEnum;
import com.github.bestheroz.standard.common.mybatis.handler.GenericEnumListTypeHandler;

public class AuthorityEnumListTypeHandler extends GenericEnumListTypeHandler<AuthorityEnum> {
  public AuthorityEnumListTypeHandler() {
    super(AuthorityEnum.class);
  }
}
