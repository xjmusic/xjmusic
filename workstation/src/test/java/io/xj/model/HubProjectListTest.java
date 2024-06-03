package io.xj.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.xj.model.pojos.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HubProjectListTest {
  private HubProjectList subject;
  private Project project1;
  private Project project2;

  @BeforeEach
  void setUp() {
    project1 = new Project();
    project1.setId(UUID.randomUUID());
    project1.setName("Test Project 1");
    project2 = new Project();
    project2.setId(UUID.randomUUID());
    project2.setName("Test Project 2");

    subject = new HubProjectList();
  }

  @Test
  void setProjects_getProjects() {
    subject.setProjects(Set.of(project1, project2));

    var output = subject.getProjects();

    assertEquals(2, output.size());
  }

  @Test
  void size() {
    subject.setProjects(Set.of(project1, project2));

    assertEquals(2, subject.size());
  }

  @Test
  void contains() {
    subject.setProjects(Set.of(project1));

    assertTrue(subject.contains(project1));
    assertFalse(subject.contains(project2));
  }

  @Test
  void add_remove() {
    subject.add(project1);

    assertTrue(subject.contains(project1));
    assertFalse(subject.contains(project2));

    subject.add(project2);

    assertTrue(subject.contains(project2));
    assertTrue(subject.contains(project2));

    subject.remove(project1);

    assertFalse(subject.contains(project1));
    assertTrue(subject.contains(project2));
  }

  @Test
  void clear() {
    subject.setProjects(Set.of(project1, project2));

    subject.clear();

    assertTrue(subject.getProjects().isEmpty());
  }

  @Test
  public void serialize_deserialize() throws JsonProcessingException {
    final ObjectMapper mapper = new ObjectMapper();
    subject.add(project1);
    subject.add(project2);
    subject.addError(new Error("test"));

    var serialized = mapper.writeValueAsString(subject);
    var deserialized = mapper.readValue(serialized, HubProjectList.class);

    assertEquals(subject.size(), deserialized.size());
    assertEquals(subject.getErrors().size(), deserialized.getErrors().size());
  }

  @Test
  void addError_getErrors() {
    subject.addError(new Error("test"));

    Collection<Error> result = subject.getErrors();

    assertEquals(1, result.size());
  }

  @Test
  void setErrors() {
    subject.setErrors(List.of(new Error("test"), new Error("test"), new Error("test")));

    Collection<Error> result = subject.getErrors();

    assertEquals(3, result.size());
  }
}
