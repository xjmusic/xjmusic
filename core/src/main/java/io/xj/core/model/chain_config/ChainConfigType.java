// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain_config;

import io.xj.core.exception.BusinessException;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;

import com.google.common.collect.ImmutableMap;

import javax.sound.sampled.AudioFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 Chain Configuration Type
 */
public enum ChainConfigType {
  OutputSampleBits,
  OutputFrameRate,
  OutputChannels,
  OutputEncoding,
  OutputContainer;

  private static final Map<ChainConfigType, String> DEFAULT_VALUES = ImmutableMap.<ChainConfigType, String>builder()
    .put(OutputSampleBits, "16")
    .put(OutputFrameRate, "48000")
    .put(OutputChannels, "2")
    .put(OutputEncoding, AudioFormat.Encoding.PCM_SIGNED.toString())
    .put(OutputContainer, "MP3")
    .build();

  /**
   cast string to chain config type enum

   @param typeString to cast to enum
   @return config type enum
   @throws BusinessException on failure
   */
  public static ChainConfigType validate(String typeString) throws BusinessException {
    if (Objects.isNull(typeString))
      throw new BusinessException("Type is required.");

    try {
      return valueOf(typeString);
    } catch (Exception e) {
      throw new BusinessException("'" + typeString + "' is not a valid type (" + CSV.joinEnum(ChainConfigType.values()) + ").", e);
    }
  }

  /**
   String Values

   @return ImmutableList of string values
   */
  public static List<String> stringValues() {
    return Text.stringValues(values());
  }

  /**
   Get default value for a chain configuration type

   @return default value
   @throws BusinessException if no default value exists
   */
  public String defaultValue() throws BusinessException {
    if (!DEFAULT_VALUES.containsKey(this))
      throw new BusinessException(String.format("No default value for type %s", this));

    return DEFAULT_VALUES.get(this);
  }

}
