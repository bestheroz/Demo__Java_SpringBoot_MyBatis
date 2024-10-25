package com.github.bestheroz.demo.entity.service;

import com.github.bestheroz.demo.entity.Admin;
import com.github.bestheroz.demo.entity.User;
import com.github.bestheroz.demo.repository.AdminRepository;
import com.github.bestheroz.demo.repository.UserRepository;
import com.github.bestheroz.standard.common.entity.IdCreatedUpdated;
import com.github.bestheroz.standard.common.enums.UserTypeEnum;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OperatorHelper {
  private final AdminRepository adminRepository;
  private final UserRepository userRepository;

  public <T extends IdCreatedUpdated> List<T> fulfilOperator(final List<T> operators) {
    Set<Long> adminIds = new HashSet<>();
    Set<Long> userIds = new HashSet<>();

    for (T operator : operators) {
      if (operator.getUpdatedObjectType().equals(UserTypeEnum.ADMIN)) {
        adminIds.add(operator.getUpdatedObjectId());
      } else if (operator.getUpdatedObjectType().equals(UserTypeEnum.USER)) {
        userIds.add(operator.getUpdatedObjectId());
      }
      if (operator.getCreatedObjectType().equals(UserTypeEnum.ADMIN)) {
        adminIds.add(operator.getCreatedObjectId());
      } else if (operator.getCreatedObjectType().equals(UserTypeEnum.USER)) {
        userIds.add(operator.getCreatedObjectId());
      }
    }

    Map<Long, Admin> adminMap =
        adminIds.isEmpty()
            ? Collections.emptyMap()
            : adminRepository
                .getTargetItemsByMap(Set.of("id", "loginId", "name"), Map.of("id:in", adminIds))
                .stream()
                .collect(Collectors.toMap(Admin::getId, Function.identity()));

    Map<Long, User> userMap =
        userIds.isEmpty()
            ? Collections.emptyMap()
            : userRepository
                .getTargetItemsByMap(Set.of("id", "loginId", "name"), Map.of("id:in", userIds))
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

    for (T operator : operators) {
      if (operator.getUpdatedObjectType().equals(UserTypeEnum.ADMIN)) {
        Admin admin = adminMap.get(operator.getUpdatedObjectId());
        if (admin != null) {
          operator.setUpdatedByAdmin(admin);
        }
      } else if (operator.getUpdatedObjectType().equals(UserTypeEnum.USER)) {
        User user = userMap.get(operator.getUpdatedObjectId());
        if (user != null) {
          operator.setUpdatedByUser(user);
        }
      }

      if (operator.getCreatedObjectType().equals(UserTypeEnum.ADMIN)) {
        Admin admin = adminMap.get(operator.getCreatedObjectId());
        if (admin != null) {
          operator.setCreatedByAdmin(admin);
        }
      } else if (operator.getCreatedObjectType().equals(UserTypeEnum.USER)) {
        User user = userMap.get(operator.getCreatedObjectId());
        if (user != null) {
          operator.setCreatedByUser(user);
        }
      }
    }

    return operators;
  }

  public <T extends IdCreatedUpdated> T fulfilOperator(final T operator) {
    return fulfilOperator(List.of(operator)).getFirst();
  }
}
