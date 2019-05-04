//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
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

  /**
   JsonObject wrapping an internal array add at a root node

   @param rootName root node name
   @param obj      to add in root node
   @return JSON object
   */
  String wrap(String rootName, Object obj);

  /**
   JsonObject wrapping an error

   @param message to wrap as error
   @return JSON object
   */
  String wrapError(String message);
}
