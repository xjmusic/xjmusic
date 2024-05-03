package io.xj.hub.pojos;


import java.io.Serializable;
import java.util.UUID;


@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class Project implements Serializable {

  private static final long serialVersionUID = 1L;

  private UUID id;
  private String name;
  private Long updatedAt;
  private String platformVersion;
  private Boolean isDeleted;

  public Project() {
  }

  public Project(Project value) {
    this.id = value.id;
    this.name = value.name;
    this.updatedAt = value.updatedAt;
    this.platformVersion = value.platformVersion;
    this.isDeleted = value.isDeleted;
  }

  public Project(
    UUID id,
    String name,
    Long updatedAt,
    String platformVersion,
    Boolean isDeleted
  ) {
    this.id = id;
    this.name = name;
    this.updatedAt = updatedAt;
    this.platformVersion = platformVersion;
    this.isDeleted = isDeleted;
  }

  /**
   Getter for <code>xj.project.id</code>.
   */
  public UUID getId() {
    return this.id;
  }

  /**
   Setter for <code>xj.project.id</code>.
   */
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   Getter for <code>xj.project.name</code>.
   */
  public String getName() {
    return this.name;
  }

  /**
   Setter for <code>xj.project.name</code>.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   Getter for <code>xj.project.updated_at</code>.
   */
  public Long getUpdatedAt() {
    return this.updatedAt;
  }

  /**
   Setter for <code>xj.project.updated_at</code>.
   */
  public void setUpdatedAt(Long updatedAt) {
    this.updatedAt = updatedAt;
  }

  /**
   Getter for <code>xj.project.platform_version</code>.
   */
  public String getPlatformVersion() {
    return this.platformVersion;
  }

  /**
   Setter for <code>xj.project.platform_version</code>.
   */
  public void setPlatformVersion(String platformVersion) {
    this.platformVersion = platformVersion;
  }

  /**
   Getter for <code>xj.project.is_deleted</code>.
   */
  public Boolean getIsDeleted() {
    return this.isDeleted;
  }

  /**
   Setter for <code>xj.project.is_deleted</code>.
   */
  public void setIsDeleted(Boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Project (");

    sb.append(id);
    sb.append(", ").append(name);
    sb.append(", ").append(updatedAt);
    sb.append(", ").append(platformVersion);
    sb.append(", ").append(isDeleted);

    sb.append(")");
    return sb.toString();
  }
}
