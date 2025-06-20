package com.github.bestheroz.demo.domain;

import com.github.bestheroz.standard.common.domain.IdCreatedUpdated;
import com.github.bestheroz.standard.common.enums.AuthorityEnum;
import com.github.bestheroz.standard.common.enums.UserTypeEnum;
import com.github.bestheroz.standard.common.security.Operator;
import com.github.bestheroz.standard.common.util.PasswordUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Column;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User extends IdCreatedUpdated {
  @Column private String loginId;
  @Column private String password;
  @Column private String token;
  @Column private String name;
  @Column private Boolean useFlag;
  @Column private List<AuthorityEnum> authorities;
  @Column private Instant changePasswordAt;
  @Column private Instant latestActiveAt;
  @Column private Instant joinedAt;
  @Column private Map<String, Object> additionalInfo;
  @Column private Boolean removedFlag;
  @Column private Instant removedAt;

  public UserTypeEnum getType() {
    return UserTypeEnum.USER;
  }

  public static User of(
      String loginId,
      String password,
      String name,
      Boolean useFlag,
      List<AuthorityEnum> authorities,
      Operator operator) {
    Instant now = Instant.now();
    User user = new User();
    user.loginId = loginId;
    user.password = PasswordUtil.getPasswordHash(password);
    user.name = name;
    user.useFlag = useFlag;
    user.authorities = authorities;
    user.joinedAt = now;
    user.additionalInfo = Map.of();
    user.removedFlag = false;
    user.setCreatedBy(operator, now);
    user.setUpdatedBy(operator, now);
    return user;
  }

  public static User of(Operator operator) {
    User user = new User();
    user.setId(operator.getId());
    user.setLoginId(operator.getLoginId());
    user.setName(operator.getName());
    return user;
  }

  public void update(
      String loginId,
      String password,
      String name,
      Boolean useFlag,
      List<AuthorityEnum> authorities,
      Operator operator) {
    this.loginId = loginId;
    this.name = name;
    this.useFlag = useFlag;
    this.authorities = authorities;
    Instant now = Instant.now();
    this.setUpdatedBy(operator, now);
    if (StringUtils.isNotEmpty(password)) {
      this.password = PasswordUtil.getPasswordHash(password);
      this.changePasswordAt = now;
    }
  }

  public void changePassword(String password, Operator operator) {
    this.password = PasswordUtil.getPasswordHash(password);
    Instant now = Instant.now();
    this.changePasswordAt = now;
    this.setUpdatedBy(operator, now);
  }

  public void remove(Operator operator) {
    this.removedFlag = true;
    Instant now = Instant.now();
    this.removedAt = now;
    this.setUpdatedBy(operator, now);
  }

  public void renewToken(String token) {
    this.token = token;
    this.latestActiveAt = Instant.now();
  }

  public void logout() {
    this.token = null;
  }
}
