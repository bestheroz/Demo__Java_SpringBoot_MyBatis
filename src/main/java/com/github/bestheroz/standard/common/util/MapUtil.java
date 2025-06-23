package com.github.bestheroz.standard.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MapUtil {
  public <K, V> Map<K, V> buildMap(Consumer<Map<K, V>> block) {
    Map<K, V> m = new HashMap<>();
    block.accept(m);
    return m;
  }
}
