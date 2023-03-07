package io.xj.nexus.fabricator;

import io.xj.hub.client.HubContent;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.entity.EntityStoreImpl;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.nexus.NexusException;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.Segment;
import io.xj.nexus.persistence.ChainManager;
import io.xj.nexus.persistence.SegmentManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FabricatorFactoryImpl implements FabricatorFactory {
  private final AppEnvironment env;
  private final ChainManager chainManager;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final SegmentManager segmentManager;
  private final JsonProvider jsonProvider;

  @Autowired
  public FabricatorFactoryImpl(
    AppEnvironment env,
    ChainManager chainManager,
    SegmentManager segmentManager,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    JsonProvider jsonProvider
  ) {
    this.env = env;
    this.chainManager = chainManager;
    this.segmentManager = segmentManager;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.jsonProvider = jsonProvider;
  }

  @Override
  public Fabricator fabricate(HubContent sourceMaterial, Segment segment) throws NexusException, FabricationFatalException {
    return new FabricatorImpl(env, sourceMaterial, segment, chainManager, this, segmentManager, jsonapiPayloadFactory, jsonProvider);
  }

  @Override
  public SegmentRetrospective loadRetrospective(Segment segment, HubContent sourceMaterial) throws NexusException, FabricationFatalException {
    return new SegmentRetrospectiveImpl(segment, segmentManager, new EntityStoreImpl());
  }

  @Override
  public SegmentWorkbench setupWorkbench(Chain chain, Segment segment) throws NexusException {
    return new SegmentWorkbenchImpl(chain, segment, segmentManager, jsonapiPayloadFactory, new EntityStoreImpl());
  }
}
