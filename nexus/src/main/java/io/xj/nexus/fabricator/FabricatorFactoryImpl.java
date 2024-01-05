// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.fabricator;

import io.xj.hub.HubContent;
import io.xj.hub.util.ValueException;
import io.xj.nexus.NexusException;
import io.xj.nexus.json.JsonProvider;
import io.xj.nexus.jsonapi.JsonapiPayloadFactory;
import io.xj.nexus.model.SegmentType;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.NexusEntityStore;
import jakarta.annotation.Nullable;

public class FabricatorFactoryImpl implements FabricatorFactory {
  final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final NexusEntityStore entityStore;
  final JsonProvider jsonProvider;

  public FabricatorFactoryImpl(
    NexusEntityStore entityStore,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    JsonProvider jsonProvider
  ) {
    this.entityStore = entityStore;
    this.jsonProvider = jsonProvider;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
  }

  @Override
  public Fabricator fabricate(HubContent sourceMaterial, Integer segmentId, double outputFrameRate, int outputChannels, @Nullable SegmentType overrideSegmentType) throws NexusException, FabricationFatalException, ManagerFatalException, ValueException {
    return new FabricatorImpl(this, entityStore, sourceMaterial, segmentId, jsonapiPayloadFactory, jsonProvider, outputFrameRate, outputChannels, overrideSegmentType);
  }

  @Override
  public SegmentRetrospective loadRetrospective(Integer segmentId) throws NexusException, FabricationFatalException {
    return new SegmentRetrospectiveImpl(entityStore, segmentId);
  }
}
