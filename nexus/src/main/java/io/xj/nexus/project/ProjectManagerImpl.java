package io.xj.nexus.project;

import io.xj.hub.HubContent;
import io.xj.hub.entity.EntityFactory;
import io.xj.hub.json.JsonProvider;
import io.xj.hub.jsonapi.JsonapiPayloadFactory;
import io.xj.hub.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Project;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.http.HttpClientProvider;
import io.xj.nexus.hub_client.HubClient;
import io.xj.nexus.hub_client.HubClientAccess;
import io.xj.nexus.hub_client.HubClientException;
import io.xj.nexus.hub_client.HubClientImpl;
import jakarta.annotation.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
  private final int downloadAudioRetries;
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
    EntityFactory entityFactory,
    int downloadAudioRetries
  ) {
    this.httpClientProvider = httpClientProvider;
    this.jsonProvider = jsonProvider;
    this.entityFactory = entityFactory;
    this.downloadAudioRetries = downloadAudioRetries;
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
  public boolean cloneProjectFromDemoTemplate(String audioBaseUrl, String parentPathPrefix, String templateShipKey, String projectName) {
    this.audioBaseUrl.set(audioBaseUrl);
    LOG.info("Cloning from demo template \"{}\" in parent folder {}", templateShipKey, parentPathPrefix);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    HubClient hubClient = new HubClientImpl(httpClientProvider, jsonProvider, jsonapiPayloadFactory);
    return cloneProject(parentPathPrefix, () -> hubClient.loadApiV1(templateShipKey, this.audioBaseUrl.get()), projectName);
  }

  @Override
  public boolean cloneFromLabProject(HubClientAccess access, String labBaseUrl, String audioBaseUrl, String parentPathPrefix, UUID projectId, String projectName) {
    this.audioBaseUrl.set(audioBaseUrl);
    LOG.info("Cloning from lab Project[{}] in parent folder {}", projectId, parentPathPrefix);
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
  public void saveProject() {
    try {
      saveProjectContent();

    } catch (IOException e) {
      LOG.error("Failed to save project!\n{}", StringUtils.formatStackTrace(e.getCause()), e);
      updateState(ProjectState.Failed);
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

  @Override
  public void notifyProjectUpdateListeners(ProjectUpdate type) {
    if (projectUpdateListeners.containsKey(type)) {
      projectUpdateListeners.get(type).forEach(Runnable::run);
    }
  }

  @Override
  public Template createTemplate(String name) throws Exception {
    var template = new Template();
    template.setId(UUID.randomUUID());
    template.setName(name);
    template.setIsDeleted(false);
    content.get().put(template);
    return template;
  }

  @Override
  public Library createLibrary(String name) throws Exception {
    var library = new Library();
    library.setId(UUID.randomUUID());
    library.setName(name);
    library.setIsDeleted(false);
    content.get().put(library);
    return library;
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

      // Don't close the client, only close the responses from it
      CloseableHttpClient httpClient = httpClientProvider.getClient();

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

            var remoteUrl = String.format("%s%s", this.audioBaseUrl, audio.getWaveformKey());
            var remoteFileSize = getRemoteFileSize(httpClient, remoteUrl);
            var localFileSize = getFileSizeIfExistsOnDisk(originalCachePath);

            boolean shouldDownload = false;

            if (localFileSize.isEmpty()) {
              shouldDownload = true;
            } else if (localFileSize.get() != remoteFileSize) {
              LOG.info("File size of {} does not match remote {} - Will download {} bytes from {}", originalCachePath, remoteFileSize, remoteFileSize, remoteUrl);
              shouldDownload = true;
            }

            if (shouldDownload) {
              if (!downloadRemoteFileWithRetry(httpClient, remoteUrl, originalCachePath, remoteFileSize)) {
                return false;
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
    LOG.info("Will save project \"{}\" to {}", projectName.get(), getPathToProjectFile());
    var json = jsonProvider.getMapper().writeValueAsString(content);
    var jsonPath = getPathToProjectFile();
    Files.writeString(Path.of(jsonPath), json);
    LOG.info("Did write {} bytes of content to {}", json.length(), jsonPath);
    updateState(ProjectState.Ready);
  }

  /**
   Get the file size on disk if it exists

   @return optional true if this dub audio cache item exists (as audio waveform data) on disk
   */
  private Optional<Integer> getFileSizeIfExistsOnDisk(String absolutePath) {
    try {
      return Optional.of((int) Files.size(Path.of(absolutePath)));
    } catch (IOException e) {
      return Optional.empty();
    }
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

  /**
   Download a file from the given URL to the given output path, retrying some number of times

   @param httpClient http client (don't close the client; only close the responses from it)
   @param url        URL to download from
   @param outputPath path to write to
   @return true if the file was downloaded successfully
   */
  private boolean downloadRemoteFileWithRetry(CloseableHttpClient httpClient, String url, String outputPath, long expectedSize) {
    for (int attempt = 1; attempt <= downloadAudioRetries; attempt++) {
      try {
        Path path = Paths.get(outputPath);
        Files.deleteIfExists(path);
        downloadRemoteFile(httpClient, url, outputPath);
        long downloadedSize = Files.size(path);
        if (downloadedSize == expectedSize) {
          return true;
        }
        LOG.info("File size does not match! Attempt " + attempt + " of " + downloadAudioRetries + " to download " + url + " to " + outputPath + " failed. Expected " + expectedSize + " bytes, but got " + downloadedSize + " bytes.");

      } catch (Exception e) {
        LOG.info("Attempt " + attempt + " of " + downloadAudioRetries + " to download " + url + " to " + outputPath + " failed because " + e.getMessage());
      }
    }
    return false;
  }

  /**
   Get the size of the file at the given URL

   @param httpClient http client (don't close the client; only close the responses from it)
   @param url        url
   @return size of the file
   @throws Exception if the file size could not be determined
   */
  private long getRemoteFileSize(CloseableHttpClient httpClient, String url) throws Exception {
    try (
      CloseableHttpResponse response = httpClient.execute(new HttpHead(url))
    ) {
      return Long.parseLong(response.getFirstHeader("Content-Length").getValue());
    } catch (Exception e) {
      throw new NexusException(String.format("Unable to get %s", url), e);
    }
  }

  /**
   Download a file from the given URL to the given output path

   @param httpClient http client (don't close the client; only close the responses from it)
   @param url        url
   @param outputPath output path
   */
  private void downloadRemoteFile(CloseableHttpClient httpClient, String url, String outputPath) throws IOException, NexusException {
    try (
      CloseableHttpResponse response = httpClient.execute(new HttpGet(url))
    ) {
      if (Objects.isNull(response.getEntity().getContent()))
        throw new NexusException(String.format("Unable to write bytes to disk: %s", outputPath));

      try (OutputStream toFile = FileUtils.openOutputStream(new File(outputPath))) {
        var size = IOUtils.copy(response.getEntity().getContent(), toFile); // stores number of bytes copied
        LOG.debug("Did write media item to disk: {} ({} bytes)", outputPath, size);
      }
    }
  }
}
