// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.audio;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.transport.CSV;

import java.util.Objects;

public enum AudioState {
  Draft,
  Published,
  Erase;

  /**
   cast string to chain config state enum
   <p>
   Maybe in future: [#294] Audio can be created in draft state, then published
   </p>

   @param stateString to cast to enum
   @return config state enum
   @throws BusinessException on failure
   */
  public static AudioState validate(String stateString) throws BusinessException {
    if (Objects.isNull(stateString))
      return Published;

    try {
      return valueOf(stateString);
    } catch (Exception e) {
      throw new BusinessException("'" + stateString + "' is not a valid state (" + CSV.joinEnum(AudioState.values()) + ").");
    }
  }
}
