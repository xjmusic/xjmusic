package io.xj.nexus.project;

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

@ExtendWith(MockitoExtension.class)
class ProjectManagerImplStructureMigrationTest {
  static final Logger LOG = LoggerFactory.getLogger(ProjectManagerImplStructureMigrationTest.class);
  private JsonProviderImpl jsonProvider;
  private ProjectManager subject;

  @Mock
  HttpClientProvider httpClientProvider;

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
    String dest = Files.createTempDirectory("LegacyExampleProject").toAbsolutePath().toString();
    LOG.info("Save project as {}", dest);

    subject.saveAsProject(dest, "NewExampleProject");

    // TODO assert all .json files and audio files exist as expected in new location
  }

  /**
   Still able to open the legacy project format, and automatically moves and renames content into the new format when saving.
   Project file structure is conducive to version control https://github.com/xjmusic/workstation/issues/335
   */
  @Test
  void saveProjectAndMigrateStructure() {
    subject.saveProject();

    // TODO assert all .json files and audio files exist as expected in current location
  }

  /**
   Project file structure is conducive to version control https://github.com/xjmusic/workstation/issues/335
   */
  @Test
  void saveProjectCleansUpUnusedJson() throws IOException {
    // TODO create an additional JSON file in the project directory
    var json = jsonProvider.getMapper().writeValueAsString(subject.getContent());
    var jsonPath = subject.getProjectPathPrefix() + "unused.json";
    Files.writeString(Path.of(jsonPath), json);


    subject.saveProject();

    // TODO assert the unused JSON file is deleted from the project directory
  }
}
