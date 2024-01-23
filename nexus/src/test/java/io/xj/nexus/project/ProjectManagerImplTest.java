package io.xj.nexus.project;

import io.xj.nexus.entity.EntityFactory;
import io.xj.nexus.entity.EntityFactoryImpl;
import io.xj.nexus.http.HttpClientProvider;
import io.xj.nexus.json.JsonProvider;
import io.xj.nexus.json.JsonProviderImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

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
}
