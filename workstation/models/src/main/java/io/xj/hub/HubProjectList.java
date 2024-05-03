package io.xj.hub;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.xj.hub.pojos.Project;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HubProjectList {
  final Set<Error> errors = new HashSet<>();
  private Collection<Project> projects;

  public HubProjectList() {
    this.projects = new HashSet<>();
  }

  public HubProjectList(Collection<Project> projects) {
    this.projects = new HashSet<>(projects);
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<Project> getProjects() {
    return projects;
  }

  public void setProjects(Collection<Project> projects) {
    this.projects = new HashSet<>(projects);
  }

  public int size() {
    return projects.size();
  }

  public boolean contains(Project project) {
    return projects.contains(project);
  }

  public boolean add(Project project) {
    return projects.add(project);
  }

  public boolean remove(Project project) {
    return projects.remove(project);
  }

  public void clear() {
    projects.clear();
  }

  /**
   Add an error to the content

   @param error to add
   @return this content object, for chaining
   */
  public HubProjectList addError(Error error) {
    errors.add(error);
    return this;
  }

  /**
   Get all errors

   @return all errors
   */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Collection<Error> getErrors() {
    return errors;
  }

  /**
   Add an error to the content

   @param errors to add
   */
  public void setErrors(List<Error> errors) {
    try {
      this.errors.clear();
      this.errors.addAll(errors);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
