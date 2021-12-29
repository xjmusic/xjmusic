// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.work;

import com.google.api.client.util.Lists;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.api.Chain;
import io.xj.api.ChainState;
import io.xj.api.Segment;
import io.xj.api.SegmentState;
import io.xj.hub.dao.Templates;
import io.xj.hub.enums.TemplateType;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.Values;
import io.xj.nexus.NexusException;
import io.xj.nexus.hub_client.client.HubClient;
import io.xj.nexus.hub_client.client.HubClientException;
import io.xj.nexus.persistence.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.lib.filestore.FileStoreProvider.EXTENSION_JSON;

@Singleton
public class NexusWorkChainManagerImpl implements NexusWorkChainManager {
  private final Logger LOG = LoggerFactory.getLogger(NexusWorkChainManagerImpl.class);
  private final AtomicReference<Instant> labPollNext;
  private final AtomicReference<Mode> mode;
  private final AtomicReference<State> state;
  private final ChainManager chains;
  private final EntityFactory entityFactory;
  private final HttpClientProvider httpClientProvider;
  private final HubClient hubClient;
  private final JsonProvider jsonProvider;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final NexusEntityStore entityStore;
  private final String shipBaseUrl;
  private final boolean enabled;
  private final int ignoreSegmentsOlderThanSeconds;
  private final int labPollSeconds;
  private final int rehydrateFabricatedAheadThreshold;

  @Nullable
  private final String shipKey;

  @Inject
  public NexusWorkChainManagerImpl(
    ChainManager chains,
    EntityFactory entityFactory,
    Environment env,
    HttpClientProvider httpClientProvider,
    HubClient hubClient,
    JsonProvider jsonProvider,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    NexusEntityStore entityStore
  ) {
    this.hubClient = hubClient;
    this.entityFactory = entityFactory;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.jsonProvider = jsonProvider;
    this.entityStore = entityStore;
    this.chains = chains;
    this.httpClientProvider = httpClientProvider;

    enabled = env.isWorkChainManagementEnabled();
    labPollSeconds = env.getWorkLabHubLabPollSeconds();
    rehydrateFabricatedAheadThreshold = env.getWorkRehydrateFabricatedAheadThreshold();
    shipBaseUrl = env.getShipBaseUrl();
    shipKey = env.getShipKey();
    ignoreSegmentsOlderThanSeconds = env.getWorkEraseSegmentsOlderThanSeconds();

    labPollNext = new AtomicReference<>(Instant.now());
    mode = new AtomicReference<>(Strings.isNullOrEmpty(shipKey) ? Mode.Lab : Mode.Yard);
    state = new AtomicReference<>(State.Init);
  }

  @Override
  public void poll() {
    if (!enabled) return;

    switch (state.get()) {
      case Init -> {
        switch (mode.get()) {
          case Lab -> state.set(State.Active);
          case Yard -> {
            state.set(State.Loading);
            if (createChainForTemplate(shipKey, TemplateType.Production))
              state.set(State.Active);
            else
              state.set(State.Fail);
          }
        }
      }

      case Loading -> {
        // no op
      }

      case Active -> {
        switch (mode.get()) {
          case Lab -> {
            if (Instant.now().isAfter(labPollNext.get())) {
              state.set(State.Loading);
              labPollNext.set(Instant.now().plusSeconds(labPollSeconds));
              if (maintainPreviewChains())
                state.set(State.Active);
              else
                state.set(State.Fail);

            } else if (!labChainsAllHealthy())
              state.set(State.Fail);
          }
          case Yard -> {
            if (!yardChainsAllHealthy())
              state.set(State.Fail);
          }
        }
      }
    }
  }

  @Override
  public boolean isHealthy() {
    return !State.Fail.equals(state.get());
  }

  /**
   Whether all lab chains are healthy

   @return true if all lab chains are healthy
   */
  private boolean labChainsAllHealthy() {
    // FUTURE test healthiness of yard chain or lab chain(s)
    return true;
  }

  /**
   Whether all yard chains are healthy

   @return true if all yard chains are healthy
   */
  private boolean yardChainsAllHealthy() {
    // FUTURE test healthiness of yard chain or yard chain(s)
    return true;
  }

  /**
   Maintain a chain for each current hub template playback

   @return true if all is well, false if something has failed
   */
  private boolean maintainPreviewChains() {
    Collection<Template> templates;
    try {
      templates = hubClient.readAllTemplatesPlaying();
    } catch (HubClientException e) {
      LOG.error("Failed to read Template Playbacks from Hub!", e);
      return false;
    }

    // Maintain chain for all templates
    Collection<Chain> allFab;
    try {
      allFab = chains.readAllFabricating();
      Set<String> chainShipKeys = allFab.stream().map(Chain::getShipKey).collect(Collectors.toSet());
      for (Template template : templates)
        if (!Strings.isNullOrEmpty(template.getShipKey()) && !chainShipKeys.contains(template.getShipKey()))
          createChainForTemplate(template.getShipKey(), TemplateType.Preview);
    } catch (ManagerFatalException | ManagerPrivilegeException e) {
      LOG.error("Failed to start Chain(s) for playing Template(s)!", e);
      return false;
    }

    // Stop chains no longer playing
    Set<String> templateShipKeys = templates.stream().map(Template::getShipKey).collect(Collectors.toSet());
    try {
      for (var chain : allFab)
        if (!templateShipKeys.contains(chain.getShipKey())) {
          LOG.info("Will stop lab Chain[{}] for no-longer-playing Template", Chains.getIdentifier(chain));
          chains.updateState(chain.getId(), ChainState.COMPLETE);
        }
    } catch (ManagerPrivilegeException | ManagerFatalException | ManagerExistenceException | ManagerValidationException e) {
      LOG.error("Failed to stop non-playing Chain(s)!", e);
      return false;
    }

    return true;
  }

  /**
   Bootstrap a chain from JSON chain bootstrap data,
   first rehydrating store from last shipped JSON matching this ship key.
   <p>
   Nexus with bootstrap chain rehydrates store on startup from shipped JSON files
   https://www.pivotaltracker.com/story/show/178718006

   @return true if successful
   */
  private Boolean createChainForTemplate(String identifier, TemplateType type) {
    Template template;
    try {
      LOG.info("Will load Template[{}]", identifier);
      template = hubClient.readTemplate(identifier);
    } catch (HubClientException e) {
      LOG.error("Failed to load Template[{}] because {}!", identifier, e.getMessage());
      return false;
    }

    // If rehydration was successful, return success
    if (rehydrateTemplate(template)) return true;

    // If the template already exists, destroy it
    chains.destroyIfExistsForShipKey(template.getShipKey());

    // Only if rehydration was unsuccessful
    try {
      LOG.info("Will bootstrap Template[{}]", Templates.getIdentifier(template));
      chains.bootstrap(type, Chains.fromTemplate(template));
      return true;
    } catch (ManagerFatalException | ManagerPrivilegeException | ManagerValidationException | ManagerExistenceException e) {
      LOG.error("Failed to bootstrap Template[{}]!", Templates.getIdentifier(template), e);
      return false;
    }
  }

  /**
   Attempt to rehydrate the store from a bootstrap, and return true if successful, so we can skip other stuff

   @param template from which to rehydrate
   @return true if the rehydration was successful
   */
  private Boolean rehydrateTemplate(Template template) {
    var success = new AtomicBoolean(true);
    Collection<Object> entities = Lists.newArrayList();
    JsonapiPayload chainPayload;
    Chain chain;

    String key = Chains.getShipKey(Chains.getFullKey(template.getShipKey()), EXTENSION_JSON);

    CloseableHttpClient client = httpClientProvider.getClient();
    try (
      CloseableHttpResponse response = client.execute(new HttpGet(String.format("%s%s", shipBaseUrl, key)))
    ) {
      LOG.debug("will check for last shipped data");
      chainPayload = jsonProvider.getMapper().readValue(response.getEntity().getContent(), JsonapiPayload.class);
      chain = jsonapiPayloadFactory.toOne(chainPayload);
      entities.add(entityFactory.clone(chain));
    } catch (JsonapiException | ClassCastException | IOException | EntityException e) {
      LOG.error("Failed to retrieve previously fabricated chain for Template[{}] because {}", template.getShipKey(), e.getMessage());
      return false;
    }

    try {
      LOG.info("Will load Chain[{}] for ship key \"{}\"", chain.getId(), template.getShipKey());
      chainPayload.getIncluded().stream()
        .filter(po -> po.isType(TemplateBinding.class))
        .forEach(templateBinding -> {
          try {
            entities.add(entityFactory.clone(jsonapiPayloadFactory.toOne(templateBinding)));
          } catch (JsonapiException | EntityException | ClassCastException e) {
            success.set(false);
            LOG.error("Could not deserialize TemplateBinding from shipped Chain JSON because {}", e.getMessage());
          }
        });

      Instant ignoreBefore = Instant.now().minusSeconds(ignoreSegmentsOlderThanSeconds);
      //noinspection DuplicatedCode
      chainPayload.getIncluded().parallelStream()
        .filter(po -> po.isType(Segment.class))
        .flatMap(po -> {
          try {
            return Stream.of((Segment) jsonapiPayloadFactory.toOne(po));
          } catch (JsonapiException | ClassCastException e) {
            LOG.error("Could not deserialize Segment from shipped Chain JSON because {}", e.getMessage());
            success.set(false);
            return Stream.empty();
          }
        })
        .filter(seg -> SegmentState.DUBBED.equals(seg.getState()))
        .filter(seg -> Instant.parse(seg.getEndAt()).isAfter(ignoreBefore))
        .forEach(segment -> {
          var segmentShipKey = Segments.getStorageFilename(segment.getStorageKey(), EXTENSION_JSON);
          try (
            CloseableHttpResponse response = client.execute(new HttpGet(String.format("%s%s", shipBaseUrl, segmentShipKey)))
          ) {
            var segmentPayload = jsonProvider.getMapper().readValue(response.getEntity().getContent(), JsonapiPayload.class);
            AtomicInteger childCount = new AtomicInteger();
            entities.add(entityFactory.clone(segment));
            segmentPayload.getIncluded()
              .forEach(po -> {
                try {
                  var entity = jsonapiPayloadFactory.toOne(po);
                  entities.add(entity);
                  childCount.getAndIncrement();
                } catch (Exception e) {
                  LOG.error("Could not deserialize Segment from shipped Chain JSON", e);
                  success.set(false);
                }
              });
            LOG.info("Read Segment[{}] and {} child entities", Segments.getIdentifier(segment), childCount);

          } catch (Exception e) {
            LOG.error("Could not load Segment[{}]", Segments.getIdentifier(segment), e);
            success.set(false);
          }
        });

      // Quit if anything failed up to here
      if (!success.get()) return false;

      // Nexus with bootstrap won't rehydrate stale Chain
      // https://www.pivotaltracker.com/story/show/178727631
      var aheadSeconds =
        Math.floor(Values.computeRelativeSeconds(Chains.computeFabricatedAheadAt(chain,
          entities.stream()
            .filter(e -> Entities.isType(e, Segment.class))
            .map(e -> (Segment) e)
            .collect(Collectors.toList()))));

      if (aheadSeconds < rehydrateFabricatedAheadThreshold) {
        LOG.info("Will not rehydrate Chain[{}] fabricated ahead {}s (not > {}s)",
          Chains.getIdentifier(chain), aheadSeconds, rehydrateFabricatedAheadThreshold);
        chains.destroy(chain.getId());
        return false;
      }

      // Okay to rehydrate
      entityStore.putAll(entities);
      LOG.info("Rehydrated {} entities OK. Chain[{}] is fabricated ahead {}s",
        entities.size(), Chains.getIdentifier(chain), aheadSeconds);
      return true;

    } catch (NexusException | ManagerFatalException | ManagerPrivilegeException | ManagerExistenceException e) {
      LOG.error("Failed to rehydrate store because {}", e.getMessage());
      return false;
    }
  }

  enum Mode {
    Lab,
    Yard,
  }

  enum State {
    Init,
    Loading,
    Active,
    Fail,
  }
}
