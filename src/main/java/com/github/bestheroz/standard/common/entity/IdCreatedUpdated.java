package com.github.bestheroz.standard.common.entity;

import com.github.bestheroz.demo.entity.Admin;
import com.github.bestheroz.demo.entity.User;
import com.github.bestheroz.standard.common.dto.UserSimpleDto;
import com.github.bestheroz.standard.common.enums.UserTypeEnum;
import com.github.bestheroz.standard.common.security.Operator;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class IdCreatedUpdated extends IdCreated {
  private UserTypeEnum updatedObjectType;

  private Instant updatedAt;
  private Long updatedObjectId;
  private Admin updatedByAdmin;
  private User updatedByUser;

  public void setUpdatedBy(Operator operator, Instant instant) {
    if (operator.getType().equals(UserTypeEnum.ADMIN)) {
      this.updatedObjectType = UserTypeEnum.ADMIN;
      this.updatedByAdmin = Admin.of(operator);
    } else if (operator.getType().equals(UserTypeEnum.USER)) {
      this.updatedObjectType = UserTypeEnum.USER;
      this.updatedByUser = User.of(operator);
    }
    this.setUpdatedAt(instant);
    this.setUpdatedObjectId(operator.getId());
    this.setUpdatedObjectType(operator.getType());
  }

  public UserSimpleDto getUpdatedBy() {
    return switch (this.updatedObjectType) {
      case ADMIN -> UserSimpleDto.of(this.updatedByAdmin);
      case USER -> UserSimpleDto.of(this.updatedByUser);
    };
  }
}
