package com.github.bestheroz.standard.common.domain;

import com.github.bestheroz.demo.domain.Admin;
import com.github.bestheroz.demo.domain.User;
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
    this.updatedAt = instant;
    this.updatedObjectId = operator.getId();
    this.updatedObjectType = operator.getType();
    if (operator.getType().equals(UserTypeEnum.ADMIN)) {
      this.updatedByAdmin = Admin.of(operator);
      this.updatedByUser = null;
    } else if (operator.getType().equals(UserTypeEnum.USER)) {
      this.updatedByAdmin = null;
      this.updatedByUser = User.of(operator);
    }
  }

  public UserSimpleDto getUpdatedBy() {
    return switch (this.updatedObjectType) {
      case ADMIN -> UserSimpleDto.of(this.updatedByAdmin);
      case USER -> UserSimpleDto.of(this.updatedByUser);
    };
  }
}
