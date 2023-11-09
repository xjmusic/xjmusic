// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.mixer;


import java.util.HashMap;
import java.util.Map;

public class EnvelopeProviderImpl implements EnvelopeProvider {
  final Map<Integer, Envelope> envelopes;

  public EnvelopeProviderImpl() {
    this.envelopes = new HashMap<>();
  }

  @Override
  public Envelope length(Integer frames) {
    if (!envelopes.containsKey(frames))
      envelopes.put(frames, new Envelope(frames));
    return envelopes.get(frames);
  }
}
