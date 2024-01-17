package io.xj.nexus.project;

import io.xj.hub.HubContent;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.Project;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.entity.EntityFactory;
import io.xj.nexus.entity.EntityFactoryImpl;
import io.xj.nexus.http.HttpClientProvider;
import io.xj.nexus.http.HttpClientProviderImpl;
import io.xj.nexus.hub_client.HubClient;
import io.xj.nexus.hub_client.HubClientAccess;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectManagerImpl implements ProjectManager {
  static final Logger LOG = LoggerFactory.getLogger(ProjectManagerImpl.class);
  private final String escapedFileSeparator = File.separator.equals("\\") ? "\\\\" : File.separator;
  private final Pattern xjProjectPathAndFilenameRgx = Pattern.compile("(.*" + escapedFileSeparator + ")([^" + escapedFileSeparator + "]+)" + ".xj$");
  private final AtomicReference<ProjectState> state = new AtomicReference<>(ProjectState.Standby);
  private final AtomicReference<Project> project = new AtomicReference<>();
  private final AtomicReference<String> projectPathPrefix = new AtomicReference<>(File.separator);
  private final AtomicReference<String> projectName = new AtomicReference<>("Project");
  private final AtomicReference<String> audioBaseUrl = new AtomicReference<>("https://audio.xj.io/");
  private final AtomicReference<HubContent> content = new AtomicReference<>();
  private final Map<ProjectUpdate, Set<Runnable>> projectUpdateListeners = new HashMap<>();
  private final JsonProvider jsonProvider;
  private final EntityFactory entityFactory;
  private final HttpClientProvider httpClientProvider;

  @Nullable
  private Consumer<Double> onProgress;

  @Nullable
  private Consumer<ProjectState> onStateChange;

  /**
   Private constructor
   */
  public ProjectManagerImpl(
    HttpClientProvider httpClientProvider,
    JsonProvider jsonProvider,
    EntityFactory entityFactory
  ) {
    this.httpClientProvider = httpClientProvider;
    this.jsonProvider = jsonProvider;
    this.entityFactory = entityFactory;
  }

  /**
   @return a new instance of the project manager
   */
  public static ProjectManager createInstance() {
    HttpClientProvider httpClientProvider = new HttpClientProviderImpl();
    JsonProvider jsonProvider = new JsonProviderImpl();
    EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
    return new ProjectManagerImpl(httpClientProvider, jsonProvider, entityFactory);
  }

  @Override
  public String getProjectPathPrefix() {
    return projectPathPrefix.get();
  }

  @Override
  public Optional<Project> getProject() {
    return Optional.ofNullable(project.get());
  }

  @Override
  public String getAudioBaseUrl() {
    return this.audioBaseUrl.get();
  }

  @Override
  public boolean cloneProjectFromDemoTemplate(String parentPathPrefix, String templateShipKey, String projectName) {
    LOG.info("Cloning from demo template \"{}\" in parent folder {}", templateShipKey, parentPathPrefix);
    HttpClientProvider httpClientProvider = new HttpClientProviderImpl();
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    HubClient hubClient = new HubClientImpl(httpClientProvider, jsonProvider, jsonapiPayloadFactory);
    return cloneProject(parentPathPrefix, () -> hubClient.loadApiV1(templateShipKey, this.audioBaseUrl.get()), projectName);
  }

  @Override
  public boolean cloneFromLabProject(HubClientAccess access, String labBaseUrl, String parentPathPrefix, UUID projectId, String projectName) {
    LOG.info("Cloning from lab Project[{}] in parent folder {}", projectId, parentPathPrefix);
    HttpClientProvider httpClientProvider = new HttpClientProviderImpl();
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    HubClient hubClient = new HubClientImpl(httpClientProvider, jsonProvider, jsonapiPayloadFactory);
    return cloneProject(parentPathPrefix, () -> hubClient.ingestApiV2(labBaseUrl, access, projectId), projectName);
  }

  @Override
  public boolean openProjectFromLocalFile(String projectFilePath) {
    LOG.info("Opening project at {}", projectFilePath);

    Matcher matcher = xjProjectPathAndFilenameRgx.matcher(projectFilePath);
    if (!matcher.find()) {
      LOG.error("Failed to parse project path prefix and name from file path: {}", projectFilePath);
      return false;
    }
    projectPathPrefix.set(matcher.group(1));
    projectName.set(matcher.group(2));

    try {
      LOG.info("Will load project \"{}\" from {}", projectName.get(), projectFilePath);
      updateState(ProjectState.LoadingContent);
      var json = Files.readString(Path.of(projectFilePath));
      content.set(jsonProvider.getMapper().readValue(json, HubContent.class));
      project.set(content.get().getProjects().stream().findFirst().orElse(null));
      updateState(ProjectState.LoadedContent);
      LOG.info("Did load content for project \"{}\" from {}", projectName.get(), projectFilePath);
      updateState(ProjectState.Ready);
      return true;

    } catch (Exception e) {
      LOG.error("Failed to open project from local file!\n{}", StringUtils.formatStackTrace(e.getCause()), e);
      updateState(ProjectState.Failed);
      return false;
    }
  }

  @Override
  public boolean createProject(String parentPathPrefix, String projectName) {
    LOG.info("Create new project \"{}\" in parent folder {}", projectName, parentPathPrefix);

    try {
      createProjectFolder(parentPathPrefix, projectName);

      // Create the new project
      project.set(new Project());
      project.get().setId(UUID.randomUUID());
      project.get().setName(projectName);

      // Create the new content
      content.set(new HubContent());
      content.get().setProjects(List.of(project.get()));

      saveProjectContent();
      return true;

    } catch (Exception e) {
      LOG.error("Failed to open project from local file!\n{}", StringUtils.formatStackTrace(e.getCause()), e);
      updateState(ProjectState.Failed);
      return false;
    }
  }

  @Override
  public void cancelProjectLoading() {
    updateState(ProjectState.Cancelled);
  }

  @Override
  public String getProjectFilename() {
    return projectName.get() + ".xj";
  }

  @Override
  public String getPathToProjectFile() {
    return projectPathPrefix.get() + getProjectFilename();
  }

  @Override
  public void setProjectPathPrefix(String projectPathPrefix) {
    this.projectPathPrefix.set(projectPathPrefix);
  }

  @Override
  public HubContent getContent() {
    return content.get();
  }

  @Override
  public String getPathToInstrumentAudio(UUID instrumentId, String waveformKey) {
    return projectPathPrefix.get() + "instrument" + File.separator + instrumentId.toString() + File.separator + waveformKey;
  }

  @Override
  public void setAudioBaseUrl(String audioBaseUrl) {

  }

  @Override
  public void setOnProgress(@Nullable Consumer<Double> onProgress) {
    this.onProgress = onProgress;
  }

  @Override
  public void setOnStateChange(@Nullable Consumer<ProjectState> onStateChange) {
    this.onStateChange = onStateChange;
  }

  @Override
  public void addProjectUpdateListener(ProjectUpdate type, Runnable listener) {
    projectUpdateListeners.computeIfAbsent(type, k -> new HashSet<>());
    projectUpdateListeners.get(type).add(listener);
  }

  @Override
  public void closeProject() {
    project.set(null);
    content.set(null);
    notifyProjectUpdateListeners(ProjectUpdate.Templates);
    notifyProjectUpdateListeners(ProjectUpdate.Libraries);
    notifyProjectUpdateListeners(ProjectUpdate.Programs);
    notifyProjectUpdateListeners(ProjectUpdate.Instruments);
  }

  /**
   Clone a project from a template

   @param parentPathPrefix parent folder of the project folder
   @param fetchContent     fetch content from the hub
   @param projectName      name of the project folder
   @return true if the project was cloned successfully
   */
  private boolean cloneProject(String parentPathPrefix, Callable<HubContent> fetchContent, String projectName) {
    try {
      createProjectFolder(parentPathPrefix, projectName);

      LOG.info("Will load content");
      updateState(ProjectState.LoadingContent);
      content.set(fetchContent.call());
      project.set(content.get().getProjects().stream().findFirst().orElse(null));
      updateState(ProjectState.LoadedContent);
      LOG.info("Did load content");

      LOG.info("Will load {} audio for {} instruments", content.get().getInstrumentAudios().size(), content.get().getInstruments().size());
      updateProgress(0.0);
      updateState(ProjectState.LoadingAudio);
      int loaded = 0;
      var instruments = new ArrayList<>(content.get().getInstruments());
      var audios = new ArrayList<>(content.get().getInstrumentAudios());
      for (Instrument instrument : instruments) {
        for (InstrumentAudio audio : audios.stream()
          .filter(a -> Objects.equals(a.getInstrumentId(), instrument.getId()))
          .sorted(Comparator.comparing(InstrumentAudio::getName))
          .toList()) {
          if (!Objects.equals(state.get(), ProjectState.LoadingAudio)) {
            // Workstation canceling preloading should cease resampling audio files https://www.pivotaltracker.com/story/show/186209135
            return false;
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
                CloseableHttpResponse response = client.execute(new HttpGet(String.format("%s%s", this.audioBaseUrl, audio.getWaveformKey())))
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

      saveProjectContent();
      return true;

    } catch (HubClientException e) {
      LOG.error("Failed to load content for project!\n{}", StringUtils.formatStackTrace(e.getCause()), e);
      updateState(ProjectState.Failed);
      return false;

    } catch (IOException e) {
      LOG.error("Failed to preload audio for project!\n{}", StringUtils.formatStackTrace(e.getCause()), e);
      updateState(ProjectState.Failed);
      return false;

    } catch (Exception e) {
      LOG.error("Failed to clone project!\n{}", StringUtils.formatStackTrace(e.getCause()), e);
      updateState(ProjectState.Failed);
      return false;
    }
  }

  /**
   Notify all listeners of a project update

   @param type the type of update
   */
  private void notifyProjectUpdateListeners(ProjectUpdate type) {
    if (projectUpdateListeners.containsKey(type)) {
      projectUpdateListeners.get(type).forEach(Runnable::run);
    }
  }

  /**
   Create the project folder on disk

   @param parentPathPrefix parent folder of the project folder
   @param projectName      name of the project folder
   @throws IOException if the project folder could not be created
   */
  private void createProjectFolder(String parentPathPrefix, String projectName) throws IOException {
    projectPathPrefix.set(parentPathPrefix + projectName + File.separator);
    this.projectName.set(projectName);

    LOG.info("Will create project folder at {}", projectPathPrefix.get());
    updateState(ProjectState.CreatingFolder);
    Files.createDirectories(Path.of(projectPathPrefix.get()));
    updateState(ProjectState.CreatedFolder);
    LOG.info("Did create project folder at {}", projectPathPrefix.get());
  }

  /**
   Save the project content to disk

   @throws IOException if the project content could not be saved
   */
  private void saveProjectContent() throws IOException {
    updateState(ProjectState.Saving);
    var json = jsonProvider.getMapper().writeValueAsString(content);
    var jsonPath = getPathToProjectFile();
    Files.writeString(Path.of(jsonPath), json);
    LOG.info("Did write {} bytes of content to {}", json.length(), jsonPath);
    updateState(ProjectState.Ready);
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
    if (state == ProjectState.Ready) {
      notifyProjectUpdateListeners(ProjectUpdate.Libraries);
      notifyProjectUpdateListeners(ProjectUpdate.Programs);
      notifyProjectUpdateListeners(ProjectUpdate.Instruments);
      notifyProjectUpdateListeners(ProjectUpdate.Templates);
    }
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
