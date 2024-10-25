package com.github.bestheroz.demo.entity.service;

import com.github.bestheroz.demo.entity.Admin;
import com.github.bestheroz.demo.entity.User;
import com.github.bestheroz.demo.repository.AdminRepository;
import com.github.bestheroz.demo.repository.UserRepository;
import com.github.bestheroz.standard.common.entity.IdCreated;
import com.github.bestheroz.standard.common.entity.IdCreatedUpdated;
import com.github.bestheroz.standard.common.enums.UserTypeEnum;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    Set<Long> adminIds =
        operators.stream()
            .filter(operator1 -> operator1.getUpdatedObjectType().equals(UserTypeEnum.ADMIN))
            .map(
                operator1 ->
                    List.of(operator1.getUpdatedObjectId(), operator1.getCreatedObjectId()))
            .flatMap(List::stream)
            .collect(Collectors.toSet());
    List<Admin> admins =
        adminIds.isEmpty()
            ? List.of()
            : adminRepository.getTargetItemsByMap(
                Set.of("id", "loginId", "name"), Map.of("id:in", adminIds));
    Set<Long> userIds =
        operators.stream()
            .filter(operator1 -> operator1.getUpdatedObjectType().equals(UserTypeEnum.USER))
            .map(
                operator1 ->
                    List.of(operator1.getUpdatedObjectId(), operator1.getCreatedObjectId()))
            .flatMap(List::stream)
            .collect(Collectors.toSet());
    List<User> users =
        userIds.isEmpty()
            ? List.of()
            : userRepository.getTargetItemsByMap(
                Set.of("id", "loginId", "name"), Map.of("id:in", userIds));
    operators.forEach(
        operator -> {
          if (!admins.isEmpty() && operator.getUpdatedObjectType().equals(UserTypeEnum.ADMIN)) {
            admins.stream()
                .filter(admin -> admin.getId().equals(operator.getUpdatedObjectId()))
                .findFirst()
                .ifPresent(operator::setUpdatedByAdmin);
          } else if (!users.isEmpty()
              && operator.getUpdatedObjectType().equals(UserTypeEnum.USER)) {
            users.stream()
                .filter(user -> user.getId().equals(operator.getUpdatedObjectId()))
                .findFirst()
                .ifPresent(operator::setUpdatedByUser);
          }
        });
    return fulfilOperatorCreated(operators, admins, users);
  }

  private <T extends IdCreated> List<T> fulfilOperatorCreated(
      final List<T> operators, final List<Admin> admins, final List<User> users) {
    List<Admin> admins_;
    if (admins == null || admins.isEmpty()) {
      Set<Long> adminIds =
          operators.stream()
              .filter(operator -> operator.getCreatedObjectType().equals(UserTypeEnum.ADMIN))
              .map(IdCreated::getCreatedObjectId)
              .collect(Collectors.toSet());
      admins_ =
          adminIds.isEmpty()
              ? List.of()
              : adminRepository.getTargetItemsByMap(
                  Set.of("id", "loginId", "name"), Map.of("id:in", adminIds));
    } else {
      admins_ = admins;
    }
    List<User> users_;
    if (users == null || users.isEmpty()) {
      Set<Long> userIds =
          operators.stream()
              .filter(operator -> operator.getCreatedObjectType().equals(UserTypeEnum.USER))
              .map(IdCreated::getCreatedObjectId)
              .collect(Collectors.toSet());
      users_ =
          userIds.isEmpty()
              ? List.of()
              : userRepository.getTargetItemsByMap(
                  Set.of("id", "loginId", "name"), Map.of("id:in", userIds));
    } else {
      users_ = users;
    }
    return operators.stream()
        .peek(
            operator -> {
              if (!admins_.isEmpty()
                  && operator.getCreatedObjectType().equals(UserTypeEnum.ADMIN)) {
                admins_.stream()
                    .filter(admin -> admin.getId().equals(operator.getCreatedObjectId()))
                    .findFirst()
                    .ifPresent(operator::setCreatedByAdmin);
              } else if (!users_.isEmpty()
                  && operator.getCreatedObjectType().equals(UserTypeEnum.USER)) {
                users_.stream()
                    .filter(user -> user.getId().equals(operator.getCreatedObjectId()))
                    .findFirst()
                    .ifPresent(operator::setCreatedByUser);
              }
            })
        .toList();
  }

  public <T extends IdCreatedUpdated> T fulfilOperator(final T operator) {
    return fulfilOperator(List.of(operator)).getFirst();
  }

  public <T extends IdCreated> T fulfilOperatorCreated(final T operator) {
    return fulfilOperatorCreated(List.of(operator), null, null).getFirst();
  }
}
