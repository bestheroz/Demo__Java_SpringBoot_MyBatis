package com.github.bestheroz.demo.entity.service;

import com.github.bestheroz.demo.entity.Admin;
import com.github.bestheroz.demo.entity.User;
import com.github.bestheroz.demo.repository.AdminRepository;
import com.github.bestheroz.demo.repository.UserRepository;
import com.github.bestheroz.standard.common.entity.IdCreated;
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

    collectIds(operators, adminIds, userIds, true);

    Map<Long, Admin> adminMap = fetchAdminMap(adminIds);
    Map<Long, User> userMap = fetchUserMap(userIds);

    setOperatorData(operators, adminMap, userMap, true);

    return operators;
  }

  public <T extends IdCreatedUpdated> T fulfilOperator(final T operator) {
    return fulfilOperator(List.of(operator)).getFirst();
  }

  public <T extends IdCreated> List<T> fulfilCreatedOperator(final List<T> operators) {
    Set<Long> adminIds = new HashSet<>();
    Set<Long> userIds = new HashSet<>();

    collectIds(operators, adminIds, userIds, false);

    Map<Long, Admin> adminMap = fetchAdminMap(adminIds);
    Map<Long, User> userMap = fetchUserMap(userIds);

    setOperatorData(operators, adminMap, userMap, false);

    return operators;
  }

  public <T extends IdCreated> T fulfilCreatedOperator(final T operator) {
    return fulfilCreatedOperator(List.of(operator)).getFirst();
  }

  private void collectIds(
      List<? extends IdCreated> operators,
      Set<Long> adminIds,
      Set<Long> userIds,
      boolean includeUpdated) {
    for (IdCreated operator : operators) {
      if (operator.getCreatedObjectType() == UserTypeEnum.ADMIN) {
        adminIds.add(operator.getCreatedObjectId());
      } else if (operator.getCreatedObjectType() == UserTypeEnum.USER) {
        userIds.add(operator.getCreatedObjectId());
      }

      if (includeUpdated && operator instanceof IdCreatedUpdated updatedOperator) {
        if (updatedOperator.getUpdatedObjectType() == UserTypeEnum.ADMIN) {
          adminIds.add(updatedOperator.getUpdatedObjectId());
        } else if (updatedOperator.getUpdatedObjectType() == UserTypeEnum.USER) {
          userIds.add(updatedOperator.getUpdatedObjectId());
        }
      }
    }
  }

  private Map<Long, Admin> fetchAdminMap(Set<Long> adminIds) {
    return adminIds.isEmpty()
        ? Collections.emptyMap()
        : adminRepository
            .getTargetItemsByMap(Set.of("id", "loginId", "name"), Map.of("id:in", adminIds))
            .stream()
            .collect(Collectors.toMap(Admin::getId, Function.identity()));
  }

  private Map<Long, User> fetchUserMap(Set<Long> userIds) {
    return userIds.isEmpty()
        ? Collections.emptyMap()
        : userRepository
            .getTargetItemsByMap(Set.of("id", "loginId", "name"), Map.of("id:in", userIds))
            .stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));
  }

  private void setOperatorData(
      List<? extends IdCreated> operators,
      Map<Long, Admin> adminMap,
      Map<Long, User> userMap,
      boolean includeUpdated) {
    for (IdCreated operator : operators) {
      if (operator.getCreatedObjectType() == UserTypeEnum.ADMIN) {
        Admin admin = adminMap.get(operator.getCreatedObjectId());
        if (admin != null) {
          operator.setCreatedByAdmin(admin);
        }
      } else if (operator.getCreatedObjectType() == UserTypeEnum.USER) {
        User user = userMap.get(operator.getCreatedObjectId());
        if (user != null) {
          operator.setCreatedByUser(user);
        }
      }

      if (includeUpdated && operator instanceof IdCreatedUpdated updatedOperator) {
        if (updatedOperator.getUpdatedObjectType() == UserTypeEnum.ADMIN) {
          Admin admin = adminMap.get(updatedOperator.getUpdatedObjectId());
          if (admin != null) {
            updatedOperator.setUpdatedByAdmin(admin);
          }
        } else if (updatedOperator.getUpdatedObjectType() == UserTypeEnum.USER) {
          User user = userMap.get(updatedOperator.getUpdatedObjectId());
          if (user != null) {
            updatedOperator.setUpdatedByUser(user);
          }
        }
      }
    }
  }
}
