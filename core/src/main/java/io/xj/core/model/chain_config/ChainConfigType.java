// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain_config;

import io.xj.core.exception.CoreException;
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
  OutputEncodingQuality,
  OutputContainer;

  private static final Map<ChainConfigType, String> DEFAULT_VALUES = ImmutableMap.<ChainConfigType, String>builder()
    .put(OutputSampleBits, "16")
    .put(OutputFrameRate, "48000")
    .put(OutputChannels, "2")
    .put(OutputEncoding, AudioFormat.Encoding.PCM_SIGNED.toString())
    .put(OutputEncodingQuality, "0.618")
    .put(OutputContainer, "OGV")
    .build();

  /**
   cast string to chain config type enum

   @param typeString to cast to enum
   @return config type enum
   @throws CoreException on failure
   */
  public static ChainConfigType validate(String typeString) throws CoreException {
    if (Objects.isNull(typeString))
      throw new CoreException("Type is required.");

    try {
      return valueOf(typeString);
    } catch (Exception e) {
      throw new CoreException("'" + typeString + "' is not a valid type (" + CSV.joinEnum(ChainConfigType.values()) + ").", e);
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
   @throws CoreException if no default value exists
   */
  public String defaultValue() throws CoreException {
    if (!DEFAULT_VALUES.containsKey(this))
      throw new CoreException(String.format("No default value for type %s", this));

    return DEFAULT_VALUES.get(this);
  }

}
