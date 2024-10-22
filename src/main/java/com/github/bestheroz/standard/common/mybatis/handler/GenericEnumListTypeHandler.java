package com.github.bestheroz.standard.common.mybatis.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

public class GenericEnumListTypeHandler<E extends Enum<E>> extends BaseTypeHandler<List<E>> {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private final Class<E> type;

  public GenericEnumListTypeHandler(Class<E> type) {
    if (type == null) throw new IllegalArgumentException("Type argument cannot be null");
    this.type = type;
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, List<E> parameter, JdbcType jdbcType)
      throws SQLException {
    try {
      // Convert enum list to list of names
      List<String> names = parameter.stream().map(Enum::name).collect(Collectors.toList());
      // Serialize to JSON
      String json = objectMapper.writeValueAsString(names);
      ps.setString(i, json);
    } catch (Exception e) {
      throw new SQLException("Error converting List<Enum> to JSON string.", e);
    }
  }

  @Override
  public List<E> getNullableResult(ResultSet rs, String columnName) throws SQLException {
    String json = rs.getString(columnName);
    return parseJsonToEnumList(json);
  }

  @Override
  public List<E> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    String json = rs.getString(columnIndex);
    return parseJsonToEnumList(json);
  }

  @Override
  public List<E> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    String json = cs.getString(columnIndex);
    return parseJsonToEnumList(json);
  }

  private List<E> parseJsonToEnumList(String json) throws SQLException {
    if (json == null || json.isEmpty()) return null;
    try {
      // Deserialize JSON to list of strings
      List<String> names = objectMapper.readValue(json, new TypeReference<List<String>>() {});
      // Convert strings to enum values
      List<E> enumList = new ArrayList<>();
      for (String name : names) {
        E enumValue = Enum.valueOf(type, name);
        enumList.add(enumValue);
      }
      return enumList;
    } catch (Exception e) {
      throw new SQLException("Error converting JSON string to List<Enum>.", e);
    }
  }
}
