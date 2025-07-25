package com.github.bestheroz.demo.services;

import com.github.bestheroz.demo.domain.User;
import com.github.bestheroz.demo.domain.service.OperatorHelper;
import com.github.bestheroz.demo.dtos.user.*;
import com.github.bestheroz.demo.repository.UserRepository;
import com.github.bestheroz.standard.common.authenticate.JwtTokenProvider;
import com.github.bestheroz.standard.common.dto.ListResult;
import com.github.bestheroz.standard.common.dto.TokenDto;
import com.github.bestheroz.standard.common.exception.AuthenticationException401;
import com.github.bestheroz.standard.common.exception.ExceptionCode;
import com.github.bestheroz.standard.common.exception.RequestException400;
import com.github.bestheroz.standard.common.security.Operator;
import com.github.bestheroz.standard.common.util.MapUtil;
import com.github.bestheroz.standard.common.util.PasswordUtil;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final OperatorHelper operatorHelper;
  private final JwtTokenProvider jwtTokenProvider;

  public ListResult<UserDto.Response> getUserList(UserDto.Request request) {
    Map<String, Object> filterMap =
        MapUtil.buildMap(
            m -> {
              m.put("removedFlag", false);
              if (request.getId() != null) {
                m.put("id", request.getId());
              }
              if (StringUtils.isNotEmpty(request.getLoginId())) {
                m.put("loginId:contains", request.getLoginId());
              }
              if (StringUtils.isNotEmpty(request.getName())) {
                m.put("name:contains", request.getName());
              }
              if (request.getUseFlag() != null) {
                m.put("useFlag", request.getUseFlag());
              }
            });

    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      CompletableFuture<Long> countFuture =
          CompletableFuture.supplyAsync(() -> userRepository.countByMap(filterMap), executor);
      CompletableFuture<List<User>> itemsFuture =
          CompletableFuture.supplyAsync(
              () ->
                  userRepository.getItemsByMapOrderByLimitOffset(
                      filterMap,
                      List.of("-id"),
                      request.getPageSize(),
                      (request.getPage() - 1) * request.getPageSize()),
              executor);
      Long count = countFuture.join();
      if (count == 0) {
        itemsFuture.cancel(true);
        return new ListResult<>(request.getPage(), request.getPageSize(), 0L, List.of());
      }
      List<User> items = itemsFuture.join();
      List<UserDto.Response> responseList =
          operatorHelper.fulfilOperator(items).stream().map(UserDto.Response::of).toList();
      return new ListResult<>(request.getPage(), request.getPageSize(), count, responseList);
    }
  }

  public UserDto.Response getUser(final Long id) {
    return this.userRepository
        .getItemById(id)
        .map((User user) -> UserDto.Response.of(operatorHelper.fulfilOperator(user)))
        .orElseThrow(() -> new RequestException400(ExceptionCode.UNKNOWN_USER));
  }

  @Transactional
  public UserDto.Response createUser(final UserCreateDto.Request request, Operator operator) {
    if (this.userRepository.countByMap(
            Map.of("loginId", request.getLoginId(), "removedFlag", false))
        > 0) {
      throw new RequestException400(ExceptionCode.ALREADY_JOINED_ACCOUNT);
    }
    User user = request.toEntity(operator);
    this.userRepository.insert(user);
    return UserDto.Response.of(operatorHelper.fulfilOperator(user));
  }

  @Transactional
  public UserDto.Response updateUser(
      final Long id, final UserUpdateDto.Request request, Operator operator) {
    User user =
        this.userRepository
            .getItemById(id)
            .orElseThrow(() -> new RequestException400(ExceptionCode.UNKNOWN_USER));
    if (user.getRemovedFlag()) throw new RequestException400(ExceptionCode.UNKNOWN_USER);

    if (this.userRepository.countByMap(
            Map.of("loginId", request.getLoginId(), "removedFlag", false, "id:not", id))
        > 0) {
      throw new RequestException400(ExceptionCode.ALREADY_JOINED_ACCOUNT);
    }

    user.update(
        request.getLoginId(),
        request.getPassword(),
        request.getName(),
        request.getUseFlag(),
        request.getAuthorities(),
        operator);
    this.userRepository.updateById(user, user.getId());
    return UserDto.Response.of(operatorHelper.fulfilOperator(user));
  }

  @Transactional
  public void deleteUser(final Long id, Operator operator) {
    User user =
        this.userRepository
            .getItemById(id)
            .orElseThrow(() -> new RequestException400(ExceptionCode.UNKNOWN_USER));
    if (user.getRemovedFlag()) throw new RequestException400(ExceptionCode.UNKNOWN_USER);
    if (user.getId().equals(operator.getId())) {
      throw new RequestException400(ExceptionCode.CANNOT_REMOVE_YOURSELF);
    }
    user.remove(operator);
    this.userRepository.updateById(user, user.getId());
  }

  @Transactional
  public UserDto.Response changePassword(
      final Long id, final UserChangePasswordDto.Request request, Operator operator) {
    User user =
        this.userRepository
            .getItemById(id)
            .orElseThrow(() -> new RequestException400(ExceptionCode.UNKNOWN_USER));
    if (user.getRemovedFlag()) throw new RequestException400(ExceptionCode.UNKNOWN_USER);
    if (!PasswordUtil.isPasswordValid(request.getOldPassword(), user.getPassword())) {
      log.warn("password not match");
      throw new RequestException400(ExceptionCode.UNKNOWN_USER);
    }
    if (PasswordUtil.isPasswordValid(request.getNewPassword(), user.getPassword())) {
      throw new RequestException400(ExceptionCode.CHANGE_TO_SAME_PASSWORD);
    }

    user.changePassword(request.getNewPassword(), operator);
    this.userRepository.updateById(user, user.getId());
    return UserDto.Response.of(operatorHelper.fulfilOperator(user));
  }

  @Transactional
  public TokenDto loginUser(UserLoginDto.Request request) {
    User user =
        this.userRepository
            .getItemByMap(Map.of("loginId", request.getLoginId(), "removedFlag", false))
            .orElseThrow(() -> new RequestException400(ExceptionCode.UNJOINED_ACCOUNT));
    if (!user.getUseFlag()) {
      throw new RequestException400(ExceptionCode.UNKNOWN_USER);
    }
    if (!PasswordUtil.isPasswordValid(request.getPassword(), user.getPassword())) {
      log.warn("password not match");
      throw new RequestException400(ExceptionCode.UNKNOWN_USER);
    }
    user.renewToken(jwtTokenProvider.createRefreshToken(new Operator(user)));
    this.userRepository.updateById(user, user.getId());
    return new TokenDto(jwtTokenProvider.createAccessToken(new Operator(user)), user.getToken());
  }

  @Transactional
  public TokenDto renewToken(String refreshToken) {
    Long id = jwtTokenProvider.getId(refreshToken);
    User user =
        this.userRepository
            .getItemById(id)
            .orElseThrow(() -> new RequestException400(ExceptionCode.UNKNOWN_USER));
    if (user.getRemovedFlag()
        || user.getToken() == null
        || !jwtTokenProvider.validateToken(refreshToken)) {
      throw new AuthenticationException401();
    }
    if (user.getToken() != null && jwtTokenProvider.issuedRefreshTokenIn3Seconds(user.getToken())) {
      return new TokenDto(jwtTokenProvider.createAccessToken(new Operator(user)), user.getToken());
    } else if (StringUtils.equals(user.getToken(), refreshToken)) {
      user.renewToken(jwtTokenProvider.createRefreshToken(new Operator(user)));
      this.userRepository.updateById(user, user.getId());
      return new TokenDto(jwtTokenProvider.createAccessToken(new Operator(user)), user.getToken());
    } else {
      throw new AuthenticationException401();
    }
  }

  @Transactional
  public void logout(Long id) {
    User user =
        this.userRepository
            .getItemById(id)
            .orElseThrow(() -> new RequestException400(ExceptionCode.UNKNOWN_USER));
    user.logout();
    this.userRepository.updateById(user, user.getId());
  }

  public Boolean checkLoginId(String loginId, Long id) {
    return this.userRepository.countByMap(
            Map.of("loginId", loginId, "removedFlag", false, "id:not", id))
        == 0;
  }
}
