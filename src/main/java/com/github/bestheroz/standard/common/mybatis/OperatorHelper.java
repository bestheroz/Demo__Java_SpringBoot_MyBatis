package com.github.bestheroz.standard.common.mybatis;

import com.github.bestheroz.demo.repository.AdminRepository;
import com.github.bestheroz.demo.repository.UserRepository;
import com.github.bestheroz.standard.common.entity.IdCreated;
import com.github.bestheroz.standard.common.entity.IdCreatedUpdated;
import com.github.bestheroz.standard.common.enums.UserTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OperatorHelper {
  private final AdminRepository adminRepository;
  private final UserRepository userRepository;

  public <T extends IdCreatedUpdated> void fulfilOperator(T operator) {
    if (operator.getUpdatedObjectType().equals(UserTypeEnum.ADMIN)) {
      adminRepository
          .getItemById(operator.getUpdatedObjectId())
          .ifPresent(operator::setUpdatedByAdmin);
    } else if (operator.getUpdatedObjectType().equals(UserTypeEnum.USER)) {
      userRepository
          .getItemById(operator.getUpdatedObjectId())
          .ifPresent(operator::setUpdatedByUser);
    }
    fulfilOperatorCreated(operator);
  }

  public <T extends IdCreated> void fulfilOperatorCreated(T operator) {
    if (operator.getCreatedObjectType().equals(UserTypeEnum.ADMIN)) {
      adminRepository
          .getItemById(operator.getCreatedObjectId())
          .ifPresent(operator::setCreatedByAdmin);
    } else if (operator.getCreatedObjectType().equals(UserTypeEnum.USER)) {
      userRepository
          .getItemById(operator.getCreatedObjectId())
          .ifPresent(operator::setCreatedByUser);
    }
  }
}
