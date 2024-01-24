package io.xj.nexus.project;

import io.xj.hub.entity.EntityFactory;
import io.xj.hub.entity.EntityFactoryImpl;
import io.xj.hub.json.JsonProvider;
import io.xj.hub.json.JsonProviderImpl;
import io.xj.nexus.http.HttpClientProvider;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectManagerImplTest {
  private final String pathToProjectFile;
  private final String baseDir;
  private ProjectManager subject;

  @Mock
  HttpClientProvider httpClientProvider;

  @Mock
  CloseableHttpClient httpClient;

  public ProjectManagerImplTest() throws URISyntaxException {
    baseDir = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("project")).toURI()).getAbsolutePath();
    pathToProjectFile = baseDir + File.separator + "test-project.xj";
  }

  @BeforeEach
  void setUp() {
    JsonProvider jsonProvider = new JsonProviderImpl();
    EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
    subject = new ProjectManagerImpl(httpClientProvider, jsonProvider, entityFactory, 3);
  }

  @Test
  void openProjectFromLocalFile() {
    assertTrue(subject.openProjectFromLocalFile(pathToProjectFile));

    assertEquals(UUID.fromString("23bcaded-2186-4697-912e-5f47bae9e9a0"), subject.getContent().getProjects().stream().findFirst().orElseThrow().getId());
    assertEquals(2, subject.getContent().getInstruments().size());
    assertEquals(2, subject.getContent().getPrograms().size());
    assertEquals(2, subject.getContent().getInstrumentMemes().size());
    assertEquals(2, subject.getContent().getInstrumentAudios().size());
    assertEquals(2, subject.getContent().getProgramMemes().size());
    assertEquals(2, subject.getContent().getProgramSequences().size());
    assertEquals(2, subject.getContent().getProgramSequencePatterns().size());
    assertEquals(2, subject.getContent().getProgramVoices().size());
    assertEquals(2, subject.getContent().getProgramVoiceTracks().size());
    assertEquals(2, subject.getContent().getProgramSequenceBindings().size());
    assertEquals(2, subject.getContent().getProgramSequencePatternEvents().size());
    assertEquals(2, subject.getContent().getProgramSequenceBindingMemes().size());
  }

  @Test
  void getProjectPathPrefix() {
    assertTrue(subject.openProjectFromLocalFile(pathToProjectFile));

    assertEquals(baseDir + File.separator, subject.getProjectPathPrefix());
  }

  @Test
  void getProject() {
    assertTrue(subject.openProjectFromLocalFile(pathToProjectFile));

    assertEquals(UUID.fromString("23bcaded-2186-4697-912e-5f47bae9e9a0"), subject.getProject().orElseThrow().getId());
  }

  @Test
  void getAudioBaseUrl() {
    subject.cloneProjectFromDemoTemplate("https://audio.test.xj.io/", "test", "test", "test");

    assertEquals("https://audio.test.xj.io/", subject.getAudioBaseUrl());
  }

  @Test
  void cloneProjectFromDemoTemplate() throws IOException {
    when(httpClientProvider.getClient()).thenReturn(httpClient);
//    when(httpClient.execute(any(HttpHead.class))).thenReturn();
    doAnswer(invocation -> {
      Object[] args = invocation.getArguments();
      HttpUriRequest request = (HttpUriRequest) args[0];

      if (true == true) {
        return null;
      } else {
        return null;
      }
    }).when(httpClient).execute(any(HttpHead.class));

    subject.cloneProjectFromDemoTemplate("https://audio.test.xj.io/", "test", "test", "test");

    // TODO test ProjectManagerImpl.cloneProjectFromDemoTemplate(..)
  }

  @Test
  void cloneFromLabProject() {
    // TODO test ProjectManagerImpl.cloneFromLabProject(..)
  }

  @Test
  void createProject() {
    // TODO test ProjectManagerImpl.createProject(..)
  }

  @Test
  void saveProject() {
    // TODO test ProjectManagerImpl.saveProject(..)
  }

  @Test
  void cancelProjectLoading() {
    // TODO test ProjectManagerImpl.cancelProjectLoading(..)
  }

  @Test
  void getProjectFilename() {
    // TODO test ProjectManagerImpl.getProjectFilename(..)
  }

  @Test
  void getPathToProjectFile() {
    // TODO test ProjectManagerImpl.getPathToProjectFile(..)
  }

  @Test
  void setProjectPathPrefix() {
    // TODO test ProjectManagerImpl.setProjectPathPrefix(..)
  }

  @Test
  void getContent() {
    // TODO test ProjectManagerImpl.getContent(..)
  }

  @Test
  void getPathToInstrumentAudio() {
    // TODO test ProjectManagerImpl.getPathToInstrumentAudio(..)
  }

  @Test
  void setOnProgress() {
    // TODO test ProjectManagerImpl.setOnProgress(..)
  }

  @Test
  void setOnStateChange() {
    // TODO test ProjectManagerImpl.setOnStateChange(..)
  }

  @Test
  void closeProject() {
    // TODO test ProjectManagerImpl.closeProject(..)
  }

  @Test
  void createTemplate() {
    // TODO test ProjectManagerImpl.createTemplate(..)
  }

  @Test
  void createLibrary() {
    // TODO test ProjectManagerImpl.createLibrary(..)
  }

  @Test
  void createProgram() {
    // TODO test ProjectManagerImpl.createProgram(..)
  }

  @Test
  void createInstrument() {
    // TODO test ProjectManagerImpl.createInstrument(..)
  }

  @Test
  void moveProgram() {
    // TODO test ProjectManagerImpl.moveProgram(..)
  }

  @Test
  void moveInstrument() {
    // TODO test ProjectManagerImpl.moveInstrument(..)
  }

  @Test
  void cloneTemplate() {
    // TODO test ProjectManagerImpl.cloneTemplate(..)
  }

  @Test
  void cloneLibrary() {
    // TODO test ProjectManagerImpl.cloneLibrary(..)
  }

  @Test
  void cloneProgram() {
    // TODO test ProjectManagerImpl.cloneProgram(..)
  }

  @Test
  void cloneProgramSequence() {
    // TODO test ProjectManagerImpl.cloneProgramSequence(..)
  }

  @Test
  void cloneProgramSequencePattern() {
    // TODO test ProjectManagerImpl.cloneProgramSequencePattern(..)
  }

  @Test
  void cloneInstrument() {
    // TODO test ProjectManagerImpl.cloneInstrument(..)
  }
}
