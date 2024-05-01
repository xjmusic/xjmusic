package io.xj.nexus.project;

import io.xj.hub.HubContent;
import io.xj.hub.HubTopology;
import io.xj.hub.entity.EntityFactory;
import io.xj.hub.entity.EntityFactoryImpl;
import io.xj.hub.json.JsonProviderImpl;
import io.xj.hub.jsonapi.JsonapiPayloadFactory;
import io.xj.hub.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.hub.util.LocalFileUtils;
import io.xj.nexus.http.HttpClientProvider;
import io.xj.nexus.hub_client.HubClientFactory;
import io.xj.nexus.hub_client.HubClientFactoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ProjectManagerImplStructureMigrationTest {
  static final Logger LOG = LoggerFactory.getLogger(ProjectManagerImplStructureMigrationTest.class);
  private JsonProviderImpl jsonProvider;
  private ProjectManager subject;

  @Mock
  HttpClientProvider httpClientProvider;

  @Spy
  Consumer<Double> onProgress;

  @BeforeEach
  void setUp() throws URISyntaxException, IOException {
    // create temporary directory
    String dest = Files.createTempDirectory("LegacyExampleProject").toAbsolutePath().toString();
    String source = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("LegacyExampleProject")).toURI()).getAbsolutePath();
    // copy recursive the testSource into the tempDir
    LocalFileUtils.copyRecursively(Paths.get(source), Paths.get(dest));

    jsonProvider = new JsonProviderImpl();
    EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
    HubTopology.buildHubApiTopology(entityFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    HubClientFactory hubClientFactory = new HubClientFactoryImpl(httpClientProvider, jsonProvider, jsonapiPayloadFactory, 3);
    subject = new ProjectManagerImpl(jsonProvider, entityFactory, httpClientProvider, hubClientFactory);
    subject.openProjectFromLocalFile(dest + File.separator + "LegacyExampleProject.xj");
  }

  /**
   Project has a Save As menu option https://github.com/xjmusic/workstation/issues/362
   Project file structure is conducive to version control https://github.com/xjmusic/workstation/issues/335
   */
  @Test
  void saveAsProjectAndMigrateStructure() throws IOException {
    subject.setOnProgress(onProgress);
    String dest = Files.createTempDirectory("test").toAbsolutePath().toString();
    LOG.info("Save project as {}", dest);

    subject.saveAsProject(dest, "NewExampleProject", "1.2.3");

    assertEquals("1.2.3", subject.getProject().orElseThrow().getPlatformVersion());
    assertEquals(dest + File.separator + "NewExampleProject" + File.separator, subject.getProjectPathPrefix());
    assertContent(1, subject.getProjectPathPrefix(), "NewExampleProject.xj");
    assertContent(3, subject.getProjectPathPrefix(), "templates", "Legacy-Template", "Legacy-Template.json");
    assertContent(1, subject.getProjectPathPrefix(), "libraries", "Legacy-Programs", "Legacy-Programs.json");
    assertContent(10, subject.getProjectPathPrefix(), "libraries", "Legacy-Programs", "Legacy-Beat", "Legacy-Beat.json");
    assertContent(9, subject.getProjectPathPrefix(), "libraries", "Legacy-Programs", "Legacy-Detail", "Legacy-Detail.json");
    assertContent(7, subject.getProjectPathPrefix(), "libraries", "Legacy-Programs", "Legacy-Macro", "Legacy-Macro.json");
    assertContent(9, subject.getProjectPathPrefix(), "libraries", "Legacy-Programs", "Legacy-Main", "Legacy-Main.json");
    assertContent(1, subject.getProjectPathPrefix(), "libraries", "Legacy-Instruments", "Legacy-Instruments.json");
    assertContent(3, subject.getProjectPathPrefix(), "libraries", "Legacy-Instruments", "Test-Instrument", "Test-Instrument.json");
    assertAudio(1084246, subject.getProjectPathPrefix(), "libraries", "Legacy-Instruments", "Test-Instrument", "Test-Instrument-Test-Ambience-Loop-X.wav");
    assertAudio(4119604, subject.getProjectPathPrefix(), "libraries", "Legacy-Instruments", "Test-Instrument", "Test-Instrument-Test-Percussion-Loop-X.wav");
    assertEquals("Test-Instrument-Test-Ambience-Loop-X.wav", subject.getContent().getInstrumentAudio(UUID.fromString("ded34218-788b-408a-8f8a-3f9cd3eab5eb")).orElseThrow().getWaveformKey());
    assertEquals("Test-Instrument-Test-Percussion-Loop-X.wav", subject.getContent().getInstrumentAudio(UUID.fromString("1e6e1f31-40c0-4ae4-bb62-faebfdda25b5")).orElseThrow().getWaveformKey());
    HubContent instrumentOnDisk = jsonProvider.getMapper().readValue(Files.readString(Path.of(subject.getProjectPathPrefix(), "libraries", "Legacy-Instruments", "Test-Instrument", "Test-Instrument.json")), HubContent.class);
    assertEquals("Test-Instrument-Test-Ambience-Loop-X.wav", instrumentOnDisk.getInstrumentAudio(UUID.fromString("ded34218-788b-408a-8f8a-3f9cd3eab5eb")).orElseThrow().getWaveformKey());
    assertEquals("Test-Instrument-Test-Percussion-Loop-X.wav", instrumentOnDisk.getInstrumentAudio(UUID.fromString("1e6e1f31-40c0-4ae4-bb62-faebfdda25b5")).orElseThrow().getWaveformKey());
  }

  /**
   Still able to open the legacy project format, and automatically moves and renames content into the new format when saving.
   Project file structure is conducive to version control https://github.com/xjmusic/workstation/issues/335
   */
  @Test
  void saveProjectAndMigrateStructure() throws IOException {
    subject.setOnProgress(onProgress);
    subject.saveProject("1.2.3");

    assertEquals("1.2.3", subject.getProject().orElseThrow().getPlatformVersion());
    assertContent(1, subject.getProjectPathPrefix(), "LegacyExampleProject.xj");
    assertContent(3, subject.getProjectPathPrefix(), "templates", "Legacy-Template", "Legacy-Template.json");
    assertContent(1, subject.getProjectPathPrefix(), "libraries", "Legacy-Programs", "Legacy-Programs.json");
    assertContent(10, subject.getProjectPathPrefix(), "libraries", "Legacy-Programs", "Legacy-Beat", "Legacy-Beat.json");
    assertContent(9, subject.getProjectPathPrefix(), "libraries", "Legacy-Programs", "Legacy-Detail", "Legacy-Detail.json");
    assertContent(7, subject.getProjectPathPrefix(), "libraries", "Legacy-Programs", "Legacy-Macro", "Legacy-Macro.json");
    assertContent(9, subject.getProjectPathPrefix(), "libraries", "Legacy-Programs", "Legacy-Main", "Legacy-Main.json");
    assertContent(1, subject.getProjectPathPrefix(), "libraries", "Legacy-Instruments", "Legacy-Instruments.json");
    assertContent(3, subject.getProjectPathPrefix(), "libraries", "Legacy-Instruments", "Test-Instrument", "Test-Instrument.json");
    assertAudio(1084246, subject.getProjectPathPrefix(), "libraries", "Legacy-Instruments", "Test-Instrument", "Test-Instrument-Test-Ambience-Loop-X.wav");
    assertAudio(4119604, subject.getProjectPathPrefix(), "libraries", "Legacy-Instruments", "Test-Instrument", "Test-Instrument-Test-Percussion-Loop-X.wav");
    assertEquals("Test-Instrument-Test-Ambience-Loop-X.wav", subject.getContent().getInstrumentAudio(UUID.fromString("ded34218-788b-408a-8f8a-3f9cd3eab5eb")).orElseThrow().getWaveformKey());
    assertEquals("Test-Instrument-Test-Percussion-Loop-X.wav", subject.getContent().getInstrumentAudio(UUID.fromString("1e6e1f31-40c0-4ae4-bb62-faebfdda25b5")).orElseThrow().getWaveformKey());
    HubContent instrumentOnDisk = jsonProvider.getMapper().readValue(Files.readString(Path.of(subject.getProjectPathPrefix(), "libraries", "Legacy-Instruments", "Test-Instrument", "Test-Instrument.json")), HubContent.class);
    assertEquals("Test-Instrument-Test-Ambience-Loop-X.wav", instrumentOnDisk.getInstrumentAudio(UUID.fromString("ded34218-788b-408a-8f8a-3f9cd3eab5eb")).orElseThrow().getWaveformKey());
    assertEquals("Test-Instrument-Test-Percussion-Loop-X.wav", instrumentOnDisk.getInstrumentAudio(UUID.fromString("1e6e1f31-40c0-4ae4-bb62-faebfdda25b5")).orElseThrow().getWaveformKey());
  }

  /**
   Only cleanup unused .xj files in the root of the project, and all unused files inside the "libraries" and "templates" folders.
   E.g. this should ignore the "render" folder in the root of the project and any other files or folders the developers want to create in their project
   Project file structure is conducive to version control https://github.com/xjmusic/workstation/issues/335
   */
  @Test
  void saveProjectCleansUpUnusedJson() throws IOException {
    subject.setOnProgress(onProgress);
    // Should cleanup these unused project content files
    var unusedProjectFile = Path.of(subject.getProjectPathPrefix() + "unused.xj");
    Files.writeString(unusedProjectFile, "test");
    Files.createDirectory(Path.of(subject.getProjectPathPrefix() + "libraries"));
    var unusedProgramsFile = Path.of(subject.getProjectPathPrefix() + "libraries", "unused.json");
    Files.writeString(unusedProgramsFile, "test");
    Files.createDirectory(Path.of(subject.getProjectPathPrefix() + "templates"));
    var unusedTemplateFile = Path.of(subject.getProjectPathPrefix() + "templates", "unused.json");
    Files.writeString(unusedTemplateFile, "test");
    Files.createDirectory(Path.of(subject.getProjectPathPrefix() + "libraries", "Legacy-Instruments"));
    Files.createDirectory(Path.of(subject.getProjectPathPrefix() + "libraries", "Legacy-Instruments", "Test-Instrument"));
    var unusedInstrumentAudioFile = Path.of(subject.getProjectPathPrefix() + "libraries", "Legacy-Instruments", "Test-Instrument", "unused.wav");
    Files.writeString(unusedInstrumentAudioFile, "test");
    // Should not cleanup any of the following files or folders
    var ignoredRenderFolder = Path.of(subject.getProjectPathPrefix() + "render");
    Files.createDirectory(ignoredRenderFolder);
    var ignoredRenderFile = Path.of(subject.getProjectPathPrefix() + "file.wav");
    Files.writeString(ignoredRenderFile, "test");
    var ignoredReadmeFile = Path.of(subject.getProjectPathPrefix() + "README.md");
    Files.writeString(ignoredReadmeFile, "test");

    subject.saveProject("1.2.3");

    assertFalse(Files.exists(unusedProjectFile));
    assertFalse(Files.exists(unusedProgramsFile));
    assertFalse(Files.exists(unusedTemplateFile));
    assertFalse(Files.exists(unusedInstrumentAudioFile));
    assertTrue(Files.exists(ignoredRenderFolder));
    assertTrue(Files.exists(ignoredRenderFile));
    assertTrue(Files.exists(ignoredReadmeFile));
  }

  /**
   Asserts the size of the content in the file at the given path.

   @param size      to assert
   @param firstPath to the file
   @param morePath  to the file
   */
  private void assertContent(int size, String firstPath, String... morePath) throws IOException {
    var json = Files.readString(Path.of(firstPath, morePath));
    var content = jsonProvider.getMapper().readValue(json, HubContent.class);
    assertEquals(size, content.size());
  }

  /**
   Asserts the size of the audio file at the given path.

   @param size      to assert
   @param firstPath to the file
   @param morePath  to the file
   */
  private void assertAudio(int size, String firstPath, String... morePath) throws IOException {
    assertEquals(size, Files.size(Path.of(firstPath, morePath)));
  }
}
