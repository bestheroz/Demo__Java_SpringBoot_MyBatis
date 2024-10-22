package com.github.bestheroz.demo.admin;

import com.github.bestheroz.demo.entity.Admin;
import com.github.bestheroz.demo.repository.AdminRepository;
import com.github.bestheroz.standard.common.authenticate.JwtTokenProvider;
import com.github.bestheroz.standard.common.dto.ListResult;
import com.github.bestheroz.standard.common.dto.TokenDto;
import com.github.bestheroz.standard.common.exception.AuthenticationException401;
import com.github.bestheroz.standard.common.exception.ExceptionCode;
import com.github.bestheroz.standard.common.exception.RequestException400;
import com.github.bestheroz.standard.common.mybatis.OperatorHelper;
import com.github.bestheroz.standard.common.security.Operator;
import com.github.bestheroz.standard.common.util.PasswordUtil;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {
  private final AdminRepository adminRepository;
  private final OperatorHelper operatorHelper;
  private final JwtTokenProvider jwtTokenProvider;

  @Transactional(readOnly = true)
  public ListResult<AdminDto.Response> getAdminList(AdminDto.Request request) {
    long count = adminRepository.countByMap(Map.of("removedFlag", false));
    List<AdminDto.Response> items =
        count == 0
            ? List.of()
            : adminRepository
                .getItemsByMapOrderByLimitOffset(
                    Map.of("removedFlag", false),
                    List.of("-id"),
                    request.getPageSize(),
                    (request.getPage() - 1) * request.getPageSize())
                .stream()
                .map(admin -> AdminDto.Response.of(admin, operatorHelper))
                .toList();
    return new ListResult<>(request.getPage(), request.getPageSize(), count, items);
  }

  @Transactional(readOnly = true)
  public AdminDto.Response getAdmin(final Long id) {
    return this.adminRepository
        .getItemById(id)
        .map(admin -> AdminDto.Response.of(admin, operatorHelper))
        .orElseThrow(() -> new RequestException400(ExceptionCode.UNKNOWN_ADMIN));
  }

  public AdminDto.Response createAdmin(final AdminCreateDto.Request request, Operator operator) {
    if (this.adminRepository.countByMap(
            Map.of("loginId", request.getLoginId(), "removedFlag", false))
        > 0) {
      throw new RequestException400(ExceptionCode.ALREADY_JOINED_ACCOUNT);
    }
    Admin admin = request.toEntity(operator);
    this.adminRepository.insert(admin);
    return AdminDto.Response.of(admin, operatorHelper);
  }

  public AdminDto.Response updateAdmin(
      final Long id, final AdminUpdateDto.Request request, Operator operator) {
    Admin admin =
        this.adminRepository
            .getItemById(id)
            .orElseThrow(() -> new RequestException400(ExceptionCode.UNKNOWN_ADMIN));
    if (admin.getRemovedFlag()) throw new RequestException400(ExceptionCode.UNKNOWN_ADMIN);
    if (!admin.getManagerFlag() && admin.getId().equals(operator.getId())) {
      throw new RequestException400(ExceptionCode.CANNOT_UPDATE_YOURSELF);
    }
    if (!admin.getManagerFlag() && !request.getManagerFlag() && !operator.getManagerFlag()) {
      throw new RequestException400(ExceptionCode.UNKNOWN_AUTHORITY);
    }

    if (this.adminRepository
        .getItemByMap(Map.of("loginId", request.getLoginId(), "removedFlag", false, "id:not", id))
        .isPresent()) {
      throw new RequestException400(ExceptionCode.ALREADY_JOINED_ACCOUNT);
    }

    admin.update(
        request.getLoginId(),
        request.getPassword(),
        request.getName(),
        request.getUseFlag(),
        request.getManagerFlag(),
        request.getAuthorities(),
        operator);
    this.adminRepository.updateById(admin, admin.getId());
    return AdminDto.Response.of(admin, operatorHelper);
  }

  public void deleteAdmin(final Long id, Operator operator) {
    Admin admin =
        this.adminRepository
            .getItemById(id)
            .orElseThrow(() -> new RequestException400(ExceptionCode.UNKNOWN_ADMIN));
    if (admin.getRemovedFlag()) throw new RequestException400(ExceptionCode.UNKNOWN_ADMIN);
    if (admin.getId().equals(operator.getId())) {
      throw new RequestException400(ExceptionCode.CANNOT_REMOVE_YOURSELF);
    }

    admin.remove(operator);
    this.adminRepository.updateById(admin, admin.getId());
  }

  public AdminDto.Response changePassword(
      final Long id, final AdminChangePasswordDto.Request request, Operator operator) {
    Admin admin =
        this.adminRepository
            .getItemById(id)
            .orElseThrow(() -> new RequestException400(ExceptionCode.UNKNOWN_ADMIN));
    if (admin.getRemovedFlag()) throw new RequestException400(ExceptionCode.UNKNOWN_ADMIN);
    if (!PasswordUtil.verifyPassword(request.getOldPassword(), admin.getPassword())) {
      log.warn("password not match");
      throw new RequestException400(ExceptionCode.UNKNOWN_ADMIN);
    }
    if (admin.getPassword().equals(request.getNewPassword())) {
      throw new RequestException400(ExceptionCode.CHANGE_TO_SAME_PASSWORD);
    }

    admin.changePassword(request.getNewPassword(), operator);
    this.adminRepository.updateById(admin, admin.getId());
    return AdminDto.Response.of(admin, operatorHelper);
  }

  public TokenDto loginAdmin(AdminLoginDto.Request request) {
    Admin admin =
        this.adminRepository
            .getItemByMap(Map.of("loginId", request.getLoginId(), "removedFlag", false))
            .orElseThrow(() -> new RequestException400(ExceptionCode.UNJOINED_ACCOUNT));
    if (!admin.getUseFlag()) {
      throw new RequestException400(ExceptionCode.UNKNOWN_ADMIN);
    }
    if (!PasswordUtil.verifyPassword(request.getPassword(), admin.getPassword())) {
      log.warn("password not match");
      throw new RequestException400(ExceptionCode.UNKNOWN_ADMIN);
    }
    admin.renewToken(jwtTokenProvider.createRefreshToken(new Operator(admin)));
    this.adminRepository.updateById(admin, admin.getId());
    return new TokenDto(jwtTokenProvider.createAccessToken(new Operator(admin)), admin.getToken());
  }

  public TokenDto renewToken(String refreshToken) {
    Long id = jwtTokenProvider.getId(refreshToken);
    Admin admin =
        this.adminRepository
            .getItemById(id)
            .orElseThrow(() -> new RequestException400(ExceptionCode.UNKNOWN_ADMIN));
    if (admin.getRemovedFlag()
        || admin.getToken() == null
        || !jwtTokenProvider.validateToken(refreshToken)) {
      throw new AuthenticationException401();
    }
    if (admin.getToken() != null
        && jwtTokenProvider.issuedRefreshTokenIn3Seconds(admin.getToken())) {
      return new TokenDto(
          jwtTokenProvider.createAccessToken(new Operator(admin)), admin.getToken());
    } else if (StringUtils.equals(admin.getToken(), refreshToken)) {
      admin.renewToken(jwtTokenProvider.createRefreshToken(new Operator(admin)));
      this.adminRepository.updateById(admin, admin.getId());
      return new TokenDto(
          jwtTokenProvider.createAccessToken(new Operator(admin)), admin.getToken());
    } else {
      throw new AuthenticationException401();
    }
  }

  public void logout(Long id) {
    Admin admin =
        this.adminRepository
            .getItemById(id)
            .orElseThrow(() -> new RequestException400(ExceptionCode.UNKNOWN_ADMIN));
    admin.logout();
    this.adminRepository.updateById(admin, admin.getId());
  }

  @Transactional(readOnly = true)
  public Boolean checkLoginId(String loginId, Long id) {
    return this.adminRepository.countByMap(
            Map.of("loginId", loginId, "removedFlag", false, "id:not", id))
        == 0;
  }
}
