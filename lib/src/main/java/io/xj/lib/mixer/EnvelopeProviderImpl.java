// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.mixer;

import com.google.common.collect.Maps;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EnvelopeProviderImpl implements EnvelopeProvider {
  final Map<Integer, Envelope> envelopes;

  public EnvelopeProviderImpl() {
    this.envelopes =  Maps.newHashMap();
  }

  @Override
  public Envelope length(Integer frames) {
    if (!envelopes.containsKey(frames))
      envelopes.put(frames, new Envelope(frames));
    return envelopes.get(frames);
  }
}
