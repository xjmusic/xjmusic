// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport;

import com.google.gson.Gson;

/**
 [#166274496] JSON transport implemented purely in Gson via GsonProvider
 */
public interface GsonProvider {

  /**
   Get the GSON instance

   @return GSON
   */
  Gson gson();

}
