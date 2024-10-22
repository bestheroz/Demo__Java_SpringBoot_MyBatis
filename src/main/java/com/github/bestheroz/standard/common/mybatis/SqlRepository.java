package com.github.bestheroz.standard.common.mybatis;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.ibatis.annotations.*;

public interface SqlRepository<T> {
  default List<T> getItems() {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        Set.of(), Set.of(), Map.of(), List.of(), null, null);
  }

  default List<T> getItemsLimitOffset(final Integer limit, final Integer offset) {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        Set.of(), Set.of(), Map.of(), List.of(), limit, offset);
  }

  default List<T> getItemsOrderBy(final List<String> orderByConditions) {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        Set.of(), Set.of(), Map.of(), orderByConditions, null, null);
  }

  default List<T> getItemsOrderByLimitOffset(
      final List<String> orderByConditions, final Integer limit, final Integer offset) {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        Set.of(), Set.of(), Map.of(), orderByConditions, limit, offset);
  }

  default List<T> getItemsByMap(final Map<String, Object> whereConditions) {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        Set.of(), Set.of(), whereConditions, List.of(), null, null);
  }

  default List<T> getItemsByMapLimitOffset(
      final Map<String, Object> whereConditions, final Integer limit, final Integer offset) {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        Set.of(), Set.of(), whereConditions, List.of(), limit, offset);
  }

  default List<T> getItemsByMapOrderBy(
      final Map<String, Object> whereConditions, final List<String> orderByConditions) {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        Set.of(), Set.of(), whereConditions, orderByConditions, null, null);
  }

  default List<T> getItemsByMapOrderByLimitOffset(
      final Map<String, Object> whereConditions,
      final List<String> orderByConditions,
      final Integer limit,
      final Integer offset) {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        Set.of(), Set.of(), whereConditions, orderByConditions, limit, offset);
  }

  default List<T> getDistinctItems(final Set<String> distinctColumns) {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        distinctColumns, Set.of(), Map.of(), List.of(), null, null);
  }

  default List<T> getDistinctItemsLimitOffset(
      final Set<String> distinctColumns, final Integer limit, final Integer offset) {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        distinctColumns, Set.of(), Map.of(), List.of(), limit, offset);
  }

  default List<T> getDistinctItemsOrderBy(
      final Set<String> distinctColumns, final List<String> orderByConditions) {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        distinctColumns, Set.of(), Map.of(), orderByConditions, null, null);
  }

  default List<T> getDistinctItemsOrderByLimitOffset(
      final Set<String> distinctColumns,
      final List<String> orderByConditions,
      final Integer limit,
      final Integer offset) {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        distinctColumns, Set.of(), Map.of(), orderByConditions, limit, offset);
  }

  default List<T> getDistinctItemsByMap(
      final Set<String> distinctColumns, final Map<String, Object> whereConditions) {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        distinctColumns, Set.of(), whereConditions, List.of(), null, null);
  }

  default List<T> getDistinctItemsByMapLimitOffset(
      final Set<String> distinctColumns,
      final Map<String, Object> whereConditions,
      final Integer limit,
      final Integer offset) {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        distinctColumns, Set.of(), whereConditions, List.of(), limit, offset);
  }

  default List<T> getDistinctItemsByMapOrderBy(
      final Set<String> distinctColumns,
      final Map<String, Object> whereConditions,
      final List<String> orderByConditions) {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        distinctColumns, Set.of(), whereConditions, orderByConditions, null, null);
  }

  default List<T> getDistinctItemsByMapOrderByLimitOffset(
      final Set<String> distinctColumns,
      final Map<String, Object> whereConditions,
      final List<String> orderByConditions,
      final Integer limit,
      final Integer offset) {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        distinctColumns, Set.of(), whereConditions, orderByConditions, limit, offset);
  }

  // Target 시리즈를 사용하기 위해서는 Entity에 반드시 @NoArgsConstructor 가 필요하다
  default List<T> getTargetItems(final Set<String> targetColumns) {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        Set.of(), targetColumns, Map.of(), List.of(), null, null);
  }

  default List<T> getTargetItemsLimitOffset(
      final Set<String> targetColumns, final Integer limit, final Integer offset) {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        Set.of(), targetColumns, Map.of(), List.of(), limit, offset);
  }

  // Target 시리즈를 사용하기 위해서는 Entity에 반드시 @NoArgsConstructor 가 필요하다
  default List<T> getTargetItemsOrderBy(
      final Set<String> targetColumns, final List<String> orderByConditions) {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        Set.of(), targetColumns, Map.of(), orderByConditions, null, null);
  }

  default List<T> getTargetItemsOrderByLimitOffset(
      final Set<String> targetColumns,
      final List<String> orderByConditions,
      final Integer limit,
      final Integer offset) {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        Set.of(), targetColumns, Map.of(), orderByConditions, limit, offset);
  }

  // Target 시리즈를 사용하기 위해서는 Entity에 반드시 @NoArgsConstructor 가 필요하다
  default List<T> getTargetItemsByMap(
      final Set<String> targetColumns, final Map<String, Object> whereConditions) {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        Set.of(), targetColumns, whereConditions, List.of(), null, null);
  }

  default List<T> getTargetItemsByMapLimitOffset(
      final Set<String> targetColumns,
      final Map<String, Object> whereConditions,
      final Integer limit,
      final Integer offset) {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        Set.of(), targetColumns, whereConditions, List.of(), limit, offset);
  }

  // Target 시리즈를 사용하기 위해서는 Entity에 반드시 @NoArgsConstructor 가 필요하다
  default List<T> getTargetItemsByMapOrderBy(
      final Set<String> targetColumns,
      final Map<String, Object> whereConditions,
      final List<String> orderByConditions) {
    return this.getTargetItemsByMapOrderByLimitOffset(
        targetColumns, whereConditions, orderByConditions, null, null);
  }

  default List<T> getTargetItemsByMapOrderByLimitOffset(
      final Set<String> targetColumns,
      final Map<String, Object> whereConditions,
      final List<String> orderByConditions,
      final Integer limit,
      final Integer offset) {
    return this.getDistinctAndTargetItemsByMapOrderByLimitOffset(
        Set.of(), targetColumns, whereConditions, orderByConditions, limit, offset);
  }

  @SelectProvider(type = SqlCommand.class, method = SqlCommand.SELECT_ITEMS)
  List<T> getDistinctAndTargetItemsByMapOrderByLimitOffset(
      final Set<String> distinctColumns,
      final Set<String> targetColumns,
      final Map<String, Object> whereConditions,
      final List<String> orderByConditions,
      final Integer limit,
      final Integer offset);

  @SelectProvider(type = SqlCommand.class, method = SqlCommand.SELECT_ITEM_BY_MAP)
  Optional<T> getItemByMap(final Map<String, Object> whereConditions);

  default Optional<T> getItemById(final Long id) {
    return this.getItemByMap(Map.of("id", id));
  }

  default long countAll() {
    return this.countByMap(Map.of());
  }

  @SelectProvider(type = SqlCommand.class, method = SqlCommand.COUNT_BY_MAP)
  long countByMap(final Map<String, Object> whereConditions);

  @InsertProvider(type = SqlCommand.class, method = SqlCommand.INSERT)
  // @SelectKey(statement = "SELECT SEQSEQSEQSEQ.NEXTVAL FROM DUAL", keyProperty = "seq", before =
  // true, resultType = Long.class)
  // insert 가 되고 나서 pk 값을 동기화하여 저장한다. pk 값이 다르다면 아래 id 를 수정
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insert(final T entity);

  @InsertProvider(type = SqlCommand.class, method = SqlCommand.INSERT_BATCH)
  // insert 가 되고 나서 pk 값을 동기화하여 저장한다. pk 값이 다르다면 아래 id 를 수정
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insertBatch(final List<T> entities);

  default void updateById(final T entity, final Long id) {
    this.updateMapByMap(SqlCommand.toMap(entity), Map.of("id", id));
  }

  default void updateByMap(final T entity, final Map<String, Object> whereConditions) {
    this.updateMapByMap(SqlCommand.toMap(entity), whereConditions);
  }

  @UpdateProvider(type = SqlCommand.class, method = SqlCommand.UPDATE_MAP_BY_MAP)
  void updateMapByMap(
      final Map<String, Object> updateMap, final Map<String, Object> whereConditions);

  default void updateMapById(final Map<String, Object> updateMap, final Long id) {
    this.updateMapByMap(updateMap, Map.of("id", id));
  }

  @DeleteProvider(type = SqlCommand.class, method = SqlCommand.DELETE_BY_MAP)
  void deleteByMap(final Map<String, Object> whereConditions);

  default void deleteById(final Long id) {
    this.deleteByMap(Map.of("id", id));
  }
}
