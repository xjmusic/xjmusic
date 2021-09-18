package io.xj.nexus.work;

import com.google.api.client.util.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import io.xj.api.Chain;
import io.xj.api.ChainState;
import io.xj.api.Segment;
import io.xj.api.SegmentState;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.hub.enums.TemplateType;
import io.xj.hub.dao.Templates;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.Value;
import io.xj.nexus.NexusException;
import io.xj.nexus.dao.ChainDAO;
import io.xj.nexus.Chains;
import io.xj.nexus.Segments;
import io.xj.nexus.dao.exception.DAOExistenceException;
import io.xj.nexus.dao.exception.DAOFatalException;
import io.xj.nexus.dao.exception.DAOPrivilegeException;
import io.xj.nexus.dao.exception.DAOValidationException;
import io.xj.nexus.hub_client.client.HubClient;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.hub_client.client.HubClientException;
import io.xj.nexus.persistence.NexusEntityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.lib.filestore.FileStoreProvider.EXTENSION_JSON;

@Singleton
public class NexusWorkChainManagerImpl implements NexusWorkChainManager {
  private final Logger LOG = LoggerFactory.getLogger(NexusWorkChainManagerImpl.class);
  private final HubClient hubClient;
  private final EntityFactory entityFactory;
  private final FileStoreProvider fileStoreProvider;
  private final int rehydrateFabricatedAheadThreshold;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final JsonProvider jsonProvider;
  private final NexusEntityStore entityStore;
  private final ChainDAO chainDAO;
  private final HubClientAccess access;
  private final AtomicReference<Mode> mode;
  private final String segmentFileBucket;
  private final AtomicReference<State> state;
  private final int labPollSeconds;
  private final AtomicReference<Instant> labPollNext;
  private final boolean enabled;

  @Nullable
  private final UUID yardTemplateId;

  @Inject
  public NexusWorkChainManagerImpl(
    ChainDAO chainDAO,
    Config config,
    EntityFactory entityFactory,
    Environment env,
    FileStoreProvider fileStoreProvider,
    HubClient hubClient,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    JsonProvider jsonProvider,
    NexusEntityStore entityStore
  ) {
    this.hubClient = hubClient;
    this.entityFactory = entityFactory;
    this.fileStoreProvider = fileStoreProvider;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.jsonProvider = jsonProvider;
    this.entityStore = entityStore;
    this.chainDAO = chainDAO;

    access = HubClientAccess.internal();
    yardTemplateId = Value.uuidOrNull(env.getBootstrapTemplateId());
    mode = new AtomicReference<>(Objects.nonNull(yardTemplateId) ? Mode.Yard : Mode.Lab);
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
        switch (mode.get()) {
          case Lab -> state.set(State.Active);
          case Yard -> {
            state.set(State.Loading);
            if (createChainForTemplate(yardTemplateId, TemplateType.Production))
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
    Collection<Chain> chains;
    try {
      chains = chainDAO.readAllFabricating(access);
      Set<UUID> chainTemplateIds = chains.stream().map(Chain::getTemplateId).collect(Collectors.toSet());
      for (Template template : templates)
        if (!chainTemplateIds.contains(template.getId()))
          createChainForTemplate(template.getId(), TemplateType.Preview);
    } catch (DAOFatalException | DAOPrivilegeException e) {
      LOG.error("Failed to start Chain(s) for playing Template(s)!", e);
      return false;
    }

    // Stop chains no longer playing
    Set<UUID> templateIds = templates.stream().map(Template::getId).collect(Collectors.toSet());
    try {
      for (var chain : chains)
        if (!templateIds.contains(chain.getTemplateId())) {
          LOG.info("Will stop lab Chain[{}] for no-longer-playing Template", Chains.getIdentifier(chain));
          chainDAO.updateState(access, chain.getId(), ChainState.COMPLETE);
        }
    } catch (DAOPrivilegeException | DAOFatalException | DAOExistenceException | DAOValidationException e) {
      LOG.error("Failed to stop non-playing Chain(s)!", e);
      return false;
    }

    return true;
  }

  /**
   Bootstrap a chain from JSON chain bootstrap data,
   first rehydrating store from last shipped JSON matching this embed key.
   <p>
   Nexus with bootstrap chain rehydrates store on startup from shipped JSON files
   https://www.pivotaltracker.com/story/show/178718006

   @return true if successful
   */
  private Boolean createChainForTemplate(UUID templateId, TemplateType type) {
    Template template;
    try {
      LOG.info("Will load Template[{}]", templateId);
      template = hubClient.readTemplate(templateId);
    } catch (HubClientException e) {
      LOG.error("Failed to load Template[{}]!", templateId, e);
      return false;
    }

    // If rehydration was successful, return success
    if (rehydrateTemplate(template)) return true;

    // If the template already exists, destroy it
    chainDAO.destroyIfExistsForEmbedKey(access, template.getEmbedKey());

    // Only if rehydration was unsuccessful
    try {
      LOG.info("Will bootstrap Template[{}]", Templates.getIdentifier(template));
      chainDAO.bootstrap(access, type, Chains.fromTemplate(template));
      return true;
    } catch (DAOFatalException | DAOPrivilegeException | DAOValidationException | DAOExistenceException e) {
      LOG.error("Failed bootstrap Template[{}]!", Templates.getIdentifier(template), e);
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
    String chainStorageKey;
    InputStream chainStream;
    JsonapiPayload chainPayload;
    Chain chain;
    try {
      LOG.info("Will check for last shipped data");
      chainStorageKey = Chains.getStorageKey(Chains.getFullKey(template.getEmbedKey()), EXTENSION_JSON);
      chainStream = fileStoreProvider.streamS3Object(segmentFileBucket, chainStorageKey);
      chainPayload = jsonProvider.getObjectMapper().readValue(chainStream, JsonapiPayload.class);
      chain = jsonapiPayloadFactory.toOne(chainPayload);
      entities.add(entityFactory.clone(chain));
    } catch (FileStoreException | JsonapiException | ClassCastException | IOException | EntityException e) {
      LOG.error("Failed to retrieve previously fabricated chain for Template[{}] because {}", template.getEmbedKey(), e.getMessage());
      return false;
    }

    try {
      LOG.info("Will load Chain[{}] for embed key \"{}\"", chain.getId(), template.getEmbedKey());
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

      chainPayload.getIncluded().stream()
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
        .forEach(segment -> {
          try {
            var segmentStorageKey = Segments.getStorageKey(segment.getStorageKey(), EXTENSION_JSON);
            var segmentStream = fileStoreProvider.streamS3Object(segmentFileBucket, segmentStorageKey);
            var segmentPayload = jsonProvider.getObjectMapper().readValue(segmentStream, JsonapiPayload.class);
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
            LOG.info("Read Segment[{}] and {} child entities", segment.getStorageKey(), childCount);

          } catch (Exception e) {
            LOG.error("Could not load Segment[{}]", segment.getStorageKey(), e);
            success.set(false);
          }
        });

      // Quit if anything failed up to here
      if (!success.get()) return false;

      // Nexus with bootstrap won't rehydrate stale Chain
      // https://www.pivotaltracker.com/story/show/178727631
      var fabricatedAheadSeconds = Chains.computeFabricatedAheadSeconds(chain,
        entities.stream()
          .filter(e -> Entities.isType(e, Segment.class))
          .map(e -> (Segment) e)
          .collect(Collectors.toList()));

      if (fabricatedAheadSeconds < rehydrateFabricatedAheadThreshold) {
        LOG.info("Will not rehydrate Chain[{}] fabricated ahead {}s (not > {}s)",
          Chains.getIdentifier(chain), fabricatedAheadSeconds, rehydrateFabricatedAheadThreshold);
        return false;
      }

      // Okay to rehydrate
      entityStore.putAll(entities);
      LOG.info("Rehydrated {} entities OK. Chain[{}] is fabricated ahead {}s",
        entities.size(), Chains.getIdentifier(chain), fabricatedAheadSeconds);
      return true;

    } catch (NexusException e) {
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
