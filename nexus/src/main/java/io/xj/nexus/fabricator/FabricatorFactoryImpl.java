// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.fabricator;

import io.xj.hub.HubContent;
import io.xj.hub.util.ValueException;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.nexus.NexusException;
import io.xj.nexus.hub_client.HubClientException;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.Segment;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.SegmentManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FabricatorFactoryImpl implements FabricatorFactory {
  final JsonapiPayloadFactory jsonapiPayloadFactory;
  final SegmentManager segmentManager;
  final JsonProvider jsonProvider;

  @Autowired
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
  public Fabricator fabricate(HubContent sourceMaterial, Segment segment) throws NexusException, FabricationFatalException, ManagerFatalException, ValueException, HubClientException {
    return new FabricatorImpl(sourceMaterial, segment, this, segmentManager, jsonapiPayloadFactory, jsonProvider);
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
