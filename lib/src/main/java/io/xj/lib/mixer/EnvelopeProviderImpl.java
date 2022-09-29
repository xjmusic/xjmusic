// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.mixer;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;

import java.util.Map;

public class EnvelopeProviderImpl implements EnvelopeProvider {
  private final Map<Integer, Envelope> envelopes;

  @Inject
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
