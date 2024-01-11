package io.xj.nexus.project;

import io.xj.hub.HubContent;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.entity.EntityFactory;
import io.xj.nexus.entity.EntityFactoryImpl;
import io.xj.nexus.http.HttpClientProvider;
import io.xj.nexus.http.HttpClientProviderImpl;
import io.xj.nexus.hub_client.HubClient;
import io.xj.nexus.hub_client.HubClientException;
import io.xj.nexus.hub_client.HubClientImpl;
import io.xj.nexus.json.JsonProvider;
import io.xj.nexus.json.JsonProviderImpl;
import io.xj.nexus.jsonapi.JsonapiPayloadFactory;
import io.xj.nexus.jsonapi.JsonapiPayloadFactoryImpl;
import jakarta.annotation.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ProjectManagerImpl implements ProjectManager {
  static final Logger LOG = LoggerFactory.getLogger(ProjectManagerImpl.class);
  private final AtomicReference<ProjectState> state = new AtomicReference<>(ProjectState.Standby);
  private final AtomicReference<String> pathPrefix = new AtomicReference<>(File.separator);
  private final AtomicReference<String> audioBaseUrl = new AtomicReference<>("https://audio.xj.io/");
  private final AtomicReference<HubContent> content = new AtomicReference<>();

  @Nullable
  private Consumer<Double> onProgress;

  @Nullable
  private Consumer<ProjectState> onStateChange;

  /**
   Private constructor
   */
  private ProjectManagerImpl(
  ) {
    // no op
  }

  /**
   @return a new instance of the project manager
   */
  public static ProjectManager createInstance() {
    return new ProjectManagerImpl();
  }

  @Override
  public String getPathPrefix() {
    return this.pathPrefix.get();
  }

  @Override
  public void setPathPrefix(String pathPrefix) {
    this.pathPrefix.set(pathPrefix);
  }

  @Override
  public String getAudioBaseUrl() {
    return this.audioBaseUrl.get();
  }

  @Override
  public void setAudioBaseUrl(String audioBaseUrl) {
    this.audioBaseUrl.set(audioBaseUrl);
  }

  @Override
  public void cloneFromDemoTemplate(String templateShipKey, String name) {
    LOG.info("Will clone from demo template \"{}\" ({}) to {}", name, templateShipKey, pathPrefix.get());

    new Thread(() -> {
      try {
        LOG.info("Will create project folder at {}", pathPrefix.get());
        updateState(ProjectState.CreatingFolder);
        Files.createDirectories(Path.of(pathPrefix.get()));
        updateState(ProjectState.CreatedFolder);
        LOG.info("Did create project folder at {}", pathPrefix.get());

        LOG.info("Will load content from demo template \"{}\"", templateShipKey);
        updateState(ProjectState.LoadingContent);
        HttpClientProvider httpClientProvider = new HttpClientProviderImpl();
        JsonProvider jsonProvider = new JsonProviderImpl();
        EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
        JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
        HubClient hubClient = new HubClientImpl(httpClientProvider, jsonProvider, jsonapiPayloadFactory);
        content.set(hubClient.load(templateShipKey, audioBaseUrl.get()));
        updateState(ProjectState.LoadedContent);
        LOG.info("Did load content from demo template \"{}\"", templateShipKey);

        LOG.info("Will load {} audio for {} instruments", content.get().getInstrumentAudios().size(), content.get().getInstruments().size());
        updateProgress(0.0);
        updateState(ProjectState.LoadingAudio);
        int loaded = 0;
        var instruments = new ArrayList<>(content.get().getInstruments());
        var audios = new ArrayList<>(content.get().getInstrumentAudios());
        // TODO Button to cancel cloning project
        // TODO When downloading each audio, check size on disk after downloading, delete and retry 3X if failed to match correct size
        // TODO When downloading each audio, if audio already exists on disk, check size on disk, delete and retry 3X if failed to match correct size
        for (Instrument instrument : instruments) {
          for (InstrumentAudio audio : audios.stream()
            .filter(a -> Objects.equals(a.getInstrumentId(), instrument.getId()))
            .sorted(Comparator.comparing(InstrumentAudio::getName))
            .toList()) {
            if (!Objects.equals(state.get(), ProjectState.LoadingAudio)) {
              // Workstation canceling preloading should cease resampling audio files https://www.pivotaltracker.com/story/show/186209135
              return;
            }
            if (!StringUtils.isNullOrEmpty(audio.getWaveformKey())) {
              LOG.debug("Will preload audio for instrument {} with waveform key {}", instrument.getName(), audio.getWaveformKey());
              // Fetch via HTTP if original does not exist
              var originalCachePath = getPathToInstrumentAudio(
                instrument.getId(),
                audio.getWaveformKey()
              );
              if (!existsOnDisk(originalCachePath)) {
                CloseableHttpClient client = httpClientProvider.getClient();
                try (
                  CloseableHttpResponse response = client.execute(new HttpGet(String.format("%s%s", audioBaseUrl, audio.getWaveformKey())))
                ) {
                  if (Objects.isNull(response.getEntity().getContent()))
                    throw new NexusException(String.format("Unable to write bytes to disk: %s", originalCachePath));

                  try (OutputStream toFile = FileUtils.openOutputStream(new File(originalCachePath))) {
                    var size = IOUtils.copy(response.getEntity().getContent(), toFile); // stores number of bytes copied
                    LOG.debug("Did write media item to disk: {} ({} bytes)", originalCachePath, size);
                  }
                } catch (NexusException | IOException e) {
                  throw new RuntimeException(e);
                }
              }
              LOG.debug("Did preload audio OK");
              updateProgress((float) loaded / audios.size());
              loaded++;
            }
          }
        }
        updateProgress(1.0);
        LOG.info("Preloaded {} audios from {} instruments", loaded, instruments.size());
        updateState(ProjectState.LoadedAudio);

        updateState(ProjectState.Saving);
        var json = jsonProvider.getMapper().writeValueAsString(content);
        var jsonPath = pathPrefix.get() + "content.json";
        Files.writeString(Path.of(jsonPath), json);
        LOG.info("Did write {} bytes of content to {}", json.length(), jsonPath);
        updateState(ProjectState.Ready);

      } catch (HubClientException e) {
        LOG.error("Failed to load content from demo template!\n{}", StringUtils.formatStackTrace(e.getCause()), e);
        updateState(ProjectState.Failed);

      } catch (IOException e) {
        LOG.error("Failed to preload audio from demo template!\n{}", StringUtils.formatStackTrace(e.getCause()), e);
        updateState(ProjectState.Failed);
      }
    }).start();
  }

  @Override
  public HubContent getContent() {
    return content.get();
  }

  @Override
  public String getPathToInstrumentAudio(UUID instrumentId, String waveformKey) {
    return pathPrefix.get() + "instrument" + File.separator + instrumentId.toString() + File.separator + waveformKey;
  }

  @Override
  public void setOnProgress(@Nullable Consumer<Double> onProgress) {
    this.onProgress = onProgress;
  }

  @Override
  public void setOnStateChange(@Nullable Consumer<ProjectState> onStateChange) {
    this.onStateChange = onStateChange;
  }

  /**
   @return true if this dub audio cache item exists (as audio waveform data) on disk
   */
  private boolean existsOnDisk(String absolutePath) {
    return new File(absolutePath).exists();
  }

  /**
   Update the state and send the updated state to the state callback

   @param state new value
   */
  private void updateState(ProjectState state) {
    this.state.set(state);
    if (Objects.nonNull(onStateChange))
      onStateChange.accept(state);
  }

  /**
   Update the progress and send the updated progress to the progress callback

   @param progress new value
   */
  private void updateProgress(double progress) {
    if (Objects.nonNull(onProgress))
      this.onProgress.accept(progress);
  }
}
