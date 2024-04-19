package io.xj.nexus.project;

import io.xj.hub.HubTopology;
import io.xj.hub.entity.EntityFactory;
import io.xj.hub.entity.EntityFactoryImpl;
import io.xj.hub.json.JsonProvider;
import io.xj.hub.json.JsonProviderImpl;
import io.xj.hub.jsonapi.JsonapiPayloadFactory;
import io.xj.hub.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.hub.util.LocalFileUtils;
import io.xj.nexus.http.HttpClientProvider;
import io.xj.nexus.hub_client.HubClientFactory;
import io.xj.nexus.hub_client.HubClientFactoryImpl;
import io.xj.nexus.work.ComplexLibraryTest;
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
import java.nio.file.Paths;
import java.util.Objects;

@ExtendWith(MockitoExtension.class)
class ProjectManagerImplStructureMigrationTest {
  static final Logger LOG = LoggerFactory.getLogger(ProjectManagerImplStructureMigrationTest.class);
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

    JsonProvider jsonProvider = new JsonProviderImpl();
    EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
    HubTopology.buildHubApiTopology(entityFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    HubClientFactory hubClientFactory = new HubClientFactoryImpl(httpClientProvider, jsonProvider, jsonapiPayloadFactory, 3);
    subject = new ProjectManagerImpl(jsonProvider, entityFactory, httpClientProvider, hubClientFactory);
    subject.openProjectFromLocalFile(dest + File.separator + "LegacyExampleProject.xj");
  }

  /**
   Project has a Save As menu option
   https://github.com/xjmusic/workstation/issues/362
   */
  @Test
  void saveAsProjectAndMigrateStructure() throws IOException {
    String dest = Files.createTempDirectory("LegacyExampleProject").toAbsolutePath().toString();
    LOG.info("Saving project to {}", dest);

    subject.saveAsProject(dest, "NewExampleProject");
  }
}
