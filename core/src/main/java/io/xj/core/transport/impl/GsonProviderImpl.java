//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.transport.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.arrangement.ArrangementSerializer;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainSerializer;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.choice.ChoiceSerializer;
import io.xj.core.model.error.Error;
import io.xj.core.model.pick.Pick;
import io.xj.core.model.pick.PickSerializer;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentFactory;
import io.xj.core.model.segment.impl.SegmentDeserializer;
import io.xj.core.model.segment.impl.SegmentInstanceCreator;
import io.xj.core.model.segment.impl.SegmentSerializer;
import io.xj.core.model.segment_chord.SegmentChord;
import io.xj.core.model.segment_chord.SegmentChordSerializer;
import io.xj.core.model.segment_meme.SegmentMeme;
import io.xj.core.model.segment_meme.SegmentMemeSerializer;
import io.xj.core.model.segment_message.SegmentMessage;
import io.xj.core.model.segment_message.SegmentMessageSerializer;
import io.xj.core.model.user.User;
import io.xj.core.model.user.UserSerializer;
import io.xj.core.time.InstantDeserializer;
import io.xj.core.time.InstantSerializer;
import io.xj.core.transport.GsonProvider;

import java.time.Instant;
import java.util.Map;

public class GsonProviderImpl implements GsonProvider {
  private final Gson gson;

  /**
   Initialize, and register all adapters@param segmentFactory
   */
  @Inject
  public GsonProviderImpl(
    SegmentFactory segmentFactory
  ) {
    GsonBuilder g = new GsonBuilder();
    g.registerTypeHierarchyAdapter(Arrangement.class, new ArrangementSerializer());
    g.registerTypeHierarchyAdapter(Chain.class, new ChainSerializer());
    g.registerTypeHierarchyAdapter(Choice.class, new ChoiceSerializer());
    g.registerTypeHierarchyAdapter(Pick.class, new PickSerializer());
    g.registerTypeHierarchyAdapter(Segment.class, new SegmentSerializer());
    g.registerTypeHierarchyAdapter(Segment.class, new SegmentDeserializer(segmentFactory));
    g.registerTypeHierarchyAdapter(SegmentChord.class, new SegmentChordSerializer());
    g.registerTypeHierarchyAdapter(SegmentMeme.class, new SegmentMemeSerializer());
    g.registerTypeHierarchyAdapter(SegmentMessage.class, new SegmentMessageSerializer());
    g.registerTypeHierarchyAdapter(Instant.class, new InstantDeserializer());
    g.registerTypeHierarchyAdapter(Instant.class, new InstantSerializer());
    g.registerTypeHierarchyAdapter(User.class, new UserSerializer());
    g.registerTypeAdapter(Segment.class, new SegmentInstanceCreator(segmentFactory));
    g.disableInnerClassSerialization();
    gson = g.create();
  }

  @Override
  public Gson gson() {
    return gson;
  }

  @Override
  public String wrap(String rootName, Object obj) {
    Map<String, Object> out = Maps.newConcurrentMap();
    out.put(rootName, obj);
    return gson().toJson(out);
  }

  @Override
  public String wrapError(String message) {
    return wrap(Error.KEY_MANY, ImmutableList.of(new Error(message)));
  }
}
