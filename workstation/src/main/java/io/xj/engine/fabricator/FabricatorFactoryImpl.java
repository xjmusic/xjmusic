// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.fabricator;

import io.xj.model.HubContent;
import io.xj.model.json.JsonProvider;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.engine.FabricationException;
import io.xj.model.enums.SegmentType;
import jakarta.annotation.Nullable;

public class FabricatorFactoryImpl implements FabricatorFactory {
  final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final SegmentEntityStore entityStore;
  final JsonProvider jsonProvider;

  public FabricatorFactoryImpl(
    SegmentEntityStore entityStore,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    JsonProvider jsonProvider
  ) {
    this.entityStore = entityStore;
    this.jsonProvider = jsonProvider;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
  }

  @Override
  public Fabricator fabricate(HubContent sourceMaterial, Integer segmentId, double outputFrameRate, int outputChannels, @Nullable SegmentType overrideSegmentType) throws FabricationException, FabricationFatalException {
    return new FabricatorImpl(this, entityStore, sourceMaterial, segmentId, jsonapiPayloadFactory, jsonProvider, outputFrameRate, outputChannels, overrideSegmentType);
  }

  @Override
  public SegmentRetrospective loadRetrospective(Integer segmentId) throws FabricationException, FabricationFatalException {
    return new SegmentRetrospectiveImpl(entityStore, segmentId);
  }
}
