// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.ship;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.xj.api.SegmentType;
import io.xj.lib.mixer.Mixer;
import io.xj.lib.mixer.MixerFactory;
import io.xj.ship.ShipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 */
public class ShipImpl implements Ship {
  private static final int MICROSECONDS_PER_SECOND = 1000000;
  private final Logger log = LoggerFactory.getLogger(ShipImpl.class);
  private final MixerFactory mixerFactory;
  private final List<String> warnings = Lists.newArrayList();
  private final Map<UUID, Float> pickOffsetStart = Maps.newHashMap();
  private Mixer mixer;

  @Inject
  public ShipImpl(
    MixerFactory mixerFactory
    /*-*/) {
    this.mixerFactory = mixerFactory;
  }

  /**
   Microseconds of seconds

   @param seconds to convert
   @return microseconds
   */
  private static Long toMicros(Double seconds) {
    return (long) (seconds * MICROSECONDS_PER_SECOND);
  }

  @Override
  public void doWork() throws ShipException {
    SegmentType type = null;
    try {

      // FUTURE ship work

    } catch (Exception e) {
      throw new ShipException("Failed to Ship", e);
    }
  }

}
