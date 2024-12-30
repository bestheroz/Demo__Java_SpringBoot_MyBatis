package com.github.bestheroz.demo.services;

import com.github.bestheroz.demo.domain.Notice;
import com.github.bestheroz.demo.domain.service.OperatorHelper;
import com.github.bestheroz.demo.dtos.notice.NoticeCreateDto;
import com.github.bestheroz.demo.dtos.notice.NoticeDto;
import com.github.bestheroz.demo.repository.NoticeRepository;
import com.github.bestheroz.standard.common.dto.ListResult;
import com.github.bestheroz.standard.common.exception.ExceptionCode;
import com.github.bestheroz.standard.common.exception.RequestException400;
import com.github.bestheroz.standard.common.security.Operator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService {
  private final NoticeRepository noticeRepository;
  private final OperatorHelper operatorHelper;

  @Transactional(readOnly = true)
  public ListResult<NoticeDto.Response> getNoticeList(NoticeDto.Request request) {
    long count = noticeRepository.countByMap(Map.of("removedFlag", false));
    return new ListResult<>(
        request.getPage(),
        request.getPageSize(),
        count,
        count == 0
            ? List.of()
            : operatorHelper
                .fulfilOperator(
                    noticeRepository.getItemsByMapOrderByLimitOffset(
                        Map.of("removedFlag", false),
                        List.of("-id"),
                        request.getPageSize(),
                        (request.getPage() - 1) * request.getPageSize()))
                .stream()
                .map(NoticeDto.Response::of)
                .toList());
  }

  @Transactional(readOnly = true)
  public NoticeDto.Response getNotice(Long id) {
    return noticeRepository
        .getItemById(id)
        .map(notice -> NoticeDto.Response.of(operatorHelper.fulfilOperator(notice)))
        .orElseThrow(() -> new RequestException400(ExceptionCode.UNKNOWN_NOTICE));
  }

  public NoticeDto.Response createNotice(NoticeCreateDto.Request request, Operator operator) {
    Notice notice = request.toEntity(operator);
    noticeRepository.insert(notice);
    return NoticeDto.Response.of(operatorHelper.fulfilOperator(notice));
  }

  public NoticeDto.Response updateNotice(
      Long id, NoticeCreateDto.Request request, Operator operator) {
    return noticeRepository
        .getItemById(id)
        .map(
            notice -> {
              notice.update(
                  request.getTitle(), request.getContent(), request.getUseFlag(), operator);
              this.noticeRepository.updateById(notice, notice.getId());
              return notice;
            })
        .map(notice -> NoticeDto.Response.of(operatorHelper.fulfilOperator(notice)))
        .orElseThrow(() -> new RequestException400(ExceptionCode.UNKNOWN_NOTICE));
  }

  public void deleteNotice(Long id, Operator operator) {
    Notice notice =
        noticeRepository
            .getItemById(id)
            .orElseThrow(() -> new RequestException400(ExceptionCode.UNKNOWN_NOTICE));
    notice.remove(operator);
    this.noticeRepository.updateById(notice, notice.getId());
  }
}
