package io.xj.model.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CollectionUtils {
  public static <E> Collection<E> reverse(Collection<E> source) {
    List<E> list = new ArrayList<>(source);
    Collections.reverse(list);
    return list;
  }
}
