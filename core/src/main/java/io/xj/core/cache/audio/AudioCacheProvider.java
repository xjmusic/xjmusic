// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.cache.audio;

public interface AudioCacheProvider {

  /**
   Get value for a particular key

   @param key to retrieve
   @return stream if cached; null if not
   */
  Item get(String key);

  /**
   Refresh the cache for a particular key

   @param key to refresh
   */
  void refresh(String key);

}
