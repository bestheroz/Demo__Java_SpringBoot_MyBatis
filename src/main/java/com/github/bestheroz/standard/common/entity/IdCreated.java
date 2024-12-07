package com.github.bestheroz.standard.common.entity;

import com.github.bestheroz.demo.entity.Admin;
import com.github.bestheroz.demo.entity.User;
import com.github.bestheroz.standard.common.dto.UserSimpleDto;
import com.github.bestheroz.standard.common.enums.UserTypeEnum;
import com.github.bestheroz.standard.common.security.Operator;
import java.time.Instant;
import lombok.Data;

@Data
public class IdCreated {
  private Long id;
  private Instant createdAt;
  private UserTypeEnum createdObjectType;
  private Long createdObjectId;
  private Admin createdByAdmin;
  private User createdByUser;

  public void setCreatedBy(Operator operator, Instant instant) {
    if (operator.getType().equals(UserTypeEnum.ADMIN)) {
      this.createdObjectType = UserTypeEnum.ADMIN;
      this.createdByAdmin = Admin.of(operator);
    } else if (operator.getType().equals(UserTypeEnum.USER)) {
      this.createdObjectType = UserTypeEnum.USER;
      this.createdByUser = User.of(operator);
    }
    this.setCreatedAt(instant);
    this.setCreatedObjectId(operator.getId());
    this.setCreatedObjectType(operator.getType());
  }

  public UserSimpleDto getCreatedBy() {
    return switch (this.createdObjectType) {
      case ADMIN -> UserSimpleDto.of(this.createdByAdmin);
      case USER -> UserSimpleDto.of(this.createdByUser);
    };
  }
}
