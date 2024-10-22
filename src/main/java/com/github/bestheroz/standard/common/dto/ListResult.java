package com.github.bestheroz.standard.common.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ListResult<T> {
  private Integer page;
  private Integer pageSize;
  private Long total;
  private List<T> items;
}
