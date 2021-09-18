package io.xj.ship.work;

import com.google.api.client.util.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.ship.persistence.ShipEntityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
public class ShipWorkChainManagerImpl implements ShipWorkChainManager {
  private final Logger LOG = LoggerFactory.getLogger(ShipWorkChainManagerImpl.class);
  private final EntityFactory entityFactory;
  private final FileStoreProvider fileStoreProvider;
  private final int rehydrateFabricatedAheadThreshold;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final JsonProvider jsonProvider;
  private final ShipEntityStore entityStore;
  private final String segmentFileBucket;
  private final AtomicReference<State> state;
  private final int labPollSeconds;
  private final AtomicReference<Instant> labPollNext;
  private final boolean enabled;

  @Nullable
  private final String bootstrapShipKey;

  @Inject
  public ShipWorkChainManagerImpl(
    Config config,
    EntityFactory entityFactory,
    Environment env,
    FileStoreProvider fileStoreProvider,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    JsonProvider jsonProvider,
    ShipEntityStore entityStore
  ) {
    this.entityFactory = entityFactory;
    this.fileStoreProvider = fileStoreProvider;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.jsonProvider = jsonProvider;
    this.entityStore = entityStore;

    bootstrapShipKey = env.getBootstrapShipKey();
    state = new AtomicReference<>(State.Init);
    segmentFileBucket = env.getSegmentFileBucket();
    rehydrateFabricatedAheadThreshold = config.getInt("work.rehydrateFabricatedAheadThreshold");
    labPollSeconds = config.getInt("work.labHubLabPollSeconds");
    enabled = config.getBoolean("work.chainManagementEnabled");
    labPollNext = new AtomicReference<>(Instant.now());
  }

  @Override
  public void poll() {
    if (!enabled) return;

    switch (state.get()) {
      case Init -> {
        state.set(State.Loading);
        if (bootstrap())
          state.set(State.Active);
        else
          state.set(State.Fail);
      }

      case Loading -> {
        // no op
      }

      case Active -> {
        if (!checkShipping())
          state.set(State.Fail);
      }
    }
  }

  /**
   Bootstrap the specified ship key

   @return true if successful
   */
  private boolean bootstrap() {
    if (Strings.isNullOrEmpty(bootstrapShipKey)) {
      LOG.error("Cannot start with null or empty bootstrap ship key!");
      return false;
    }

    // FUTURE create a shipper for the specified bootstrap

    return true;
  }

  /**
   FUTURE: test actual health of shipping

   @return true if shipping is healthy
   */
  private boolean checkShipping() {
    return true;
  }

  @Override
  public boolean isHealthy() {
    return !State.Fail.equals(state.get());
  }

  enum State {
    Init,
    Loading,
    Active,
    Fail,
  }
}
