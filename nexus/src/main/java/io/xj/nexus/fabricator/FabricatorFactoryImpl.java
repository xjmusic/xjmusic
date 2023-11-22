// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.fabricator;

import io.xj.hub.HubContent;
import io.xj.hub.util.ValueException;
import io.xj.nexus.json.JsonProvider;
import io.xj.nexus.jsonapi.JsonapiPayloadFactory;
import io.xj.nexus.NexusException;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.Segment;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.SegmentManager;

public class FabricatorFactoryImpl implements FabricatorFactory {
  final JsonapiPayloadFactory jsonapiPayloadFactory;
  final SegmentManager segmentManager;
  final JsonProvider jsonProvider;

  public FabricatorFactoryImpl(
    SegmentManager segmentManager,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    JsonProvider jsonProvider
  ) {
    this.jsonProvider = jsonProvider;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.segmentManager = segmentManager;
  }

  @Override
  public Fabricator fabricate(HubContent sourceMaterial, Segment segment, double outputFrameRate, int outputChannels) throws NexusException, FabricationFatalException, ManagerFatalException, ValueException {
    return new FabricatorImpl(sourceMaterial, segment, this, segmentManager, jsonapiPayloadFactory, jsonProvider, outputFrameRate, outputChannels);
  }

  @Override
  public SegmentRetrospective loadRetrospective(Segment segment, HubContent sourceMaterial) throws NexusException, FabricationFatalException {
    return new SegmentRetrospectiveImpl(segment, segmentManager);
  }

  @Override
  public SegmentWorkbench setupWorkbench(Chain chain, Segment segment) throws NexusException {
    return new SegmentWorkbenchImpl(chain, segment, segmentManager, jsonapiPayloadFactory);
  }

}
