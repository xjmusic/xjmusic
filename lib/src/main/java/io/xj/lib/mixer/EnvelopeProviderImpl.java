// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.mixer;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EnvelopeProviderImpl implements EnvelopeProvider {
  final Map<Integer, Envelope> envelopes;

  public EnvelopeProviderImpl() {
    this.envelopes = new HashMap<>();
  }

  @Override
  public Envelope length(Integer frames) {
    if (!envelopes.containsKey(frames)){
      Envelope envelope = new Envelope(frames);
      envelopes.put(frames, envelope);
      return envelope;
    }
    return envelopes.get(frames);
  }
}
