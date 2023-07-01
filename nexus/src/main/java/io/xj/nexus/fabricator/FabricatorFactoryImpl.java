package io.xj.nexus.fabricator;

import io.xj.hub.client.HubContent;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.nexus.NexusException;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.Segment;
import io.xj.nexus.persistence.ChainManager;
import io.xj.nexus.persistence.FilePathProvider;
import io.xj.nexus.persistence.SegmentManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FabricatorFactoryImpl implements FabricatorFactory {
  private final ChainManager chainManager;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final SegmentManager segmentManager;
  private final JsonProvider jsonProvider;
  private final FilePathProvider filePathProvider;

  @Autowired
  public FabricatorFactoryImpl(
    ChainManager chainManager,
    SegmentManager segmentManager,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    JsonProvider jsonProvider,
    FilePathProvider filePathProvider
  ) {
    this.chainManager = chainManager;
    this.jsonProvider = jsonProvider;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.segmentManager = segmentManager;
    this.filePathProvider = filePathProvider;
  }

  @Override
  public Fabricator fabricate(HubContent sourceMaterial, Segment segment) throws NexusException, FabricationFatalException {
    return new FabricatorImpl(sourceMaterial, segment, chainManager, this, segmentManager, jsonapiPayloadFactory, jsonProvider,false);
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
