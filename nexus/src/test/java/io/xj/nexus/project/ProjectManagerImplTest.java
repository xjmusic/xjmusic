package io.xj.nexus.project;

import io.xj.hub.entity.EntityFactory;
import io.xj.hub.entity.EntityFactoryImpl;
import io.xj.hub.json.JsonProvider;
import io.xj.hub.json.JsonProviderImpl;
import io.xj.nexus.http.HttpClientProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ProjectManagerImplTest {
  ProjectManager subject;

  @Mock
  HttpClientProvider httpClientProvider;

  @BeforeEach
  void setUp() {
    JsonProvider jsonProvider = new JsonProviderImpl();
    EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
    subject = new ProjectManagerImpl(httpClientProvider, jsonProvider, entityFactory, 3);
  }

  @Test
  void openProjectFromLocalFile() {
    var testProjectResource = getClass().getResource("/test-project.xj");
    assertNotNull(testProjectResource);

    subject.openProjectFromLocalFile(testProjectResource.getPath());
  }

  @Test
  void getProjectPathPrefix() {
    // TODO test ProjectManagerImpl.getProjectPathPrefix(..)
  }

  @Test
  void getProject() {
    // TODO test ProjectManagerImpl.getProject(..)
  }

  @Test
  void getAudioBaseUrl() {
    // TODO test ProjectManagerImpl.getAudioBaseUrl(..)
  }

  @Test
  void cloneProjectFromDemoTemplate() {
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
