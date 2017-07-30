// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.audio;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;

import java.util.Objects;

public enum AudioState {
  Draft,
  Published,
  Erase;

  /**
   cast string to enum
   <p>
   Maybe in future: [#294] Audio can be created in draft state, then published
   </p>

   @param value to cast to enum
   @return config state enum
   @throws BusinessException on failure
   */
  public static AudioState validate(String value) throws BusinessException {
    if (Objects.isNull(value))
      return Published;

    try {
      return valueOf(Text.ProperSlug(value));
    } catch (Exception e) {
      throw new BusinessException("'" + value + "' is not a valid state (" + CSV.joinEnum(AudioState.values()) + ").");
    }
  }
}
