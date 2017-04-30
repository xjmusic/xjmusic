package io.outright.xj.core.util;

import com.google.common.base.CaseFormat;

public interface CamelCasify {

  /**
   Convert underscore to camelcase only if underscores are present, else return original key.

   @param key to convert.
   @return converted or original key.
   */
  static String ifNeeded(String key) {
    return key.contains("_") ?
      CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, key) :
      key;
  }

  /**
   Convert underscore to upper-camelcase only if underscores are present, else return original key.

   @param key to convert.
   @return converted or original key.
   */
  static String ifNeededUpper(String key) {
    return key.contains("_") ?
      CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, key) :
      key;
  }

}
