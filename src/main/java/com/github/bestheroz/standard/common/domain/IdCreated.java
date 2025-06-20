package com.github.bestheroz.standard.common.domain;

import com.github.bestheroz.demo.domain.Admin;
import com.github.bestheroz.demo.domain.User;
import com.github.bestheroz.standard.common.dto.UserSimpleDto;
import com.github.bestheroz.standard.common.enums.UserTypeEnum;
import com.github.bestheroz.standard.common.security.Operator;
import jakarta.persistence.Column;
import java.time.Instant;
import lombok.Data;

@Data
public class IdCreated {
  @Column private Long id;
  @Column private Instant createdAt;
  @Column private UserTypeEnum createdObjectType;
  @Column private Long createdObjectId;
  private Admin createdByAdmin;
  private User createdByUser;

  public void setCreatedBy(Operator operator, Instant instant) {
    this.createdAt = instant;
    this.createdObjectType = operator.getType();
    this.createdObjectId = operator.getId();
    if (operator.getType().equals(UserTypeEnum.ADMIN)) {
      this.createdByAdmin = Admin.of(operator);
      this.createdByUser = null;
    } else if (operator.getType().equals(UserTypeEnum.USER)) {
      this.createdByAdmin = null;
      this.createdByUser = User.of(operator);
    }
  }

  public UserSimpleDto getCreatedBy() {
    return switch (this.createdObjectType) {
      case ADMIN -> UserSimpleDto.of(this.createdByAdmin);
      case USER -> UserSimpleDto.of(this.createdByUser);
    };
  }
}
