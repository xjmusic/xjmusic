package io.xj.model.pojos;


import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class Template implements Serializable, Comparable<Template> {

  private static final long serialVersionUID = 1L;

  private UUID id;
  private UUID projectId;
  private String name;
  private String config;
  private String shipKey;
  private Boolean isDeleted;
  private Long updatedAt;

  public Template() {
  }

  public Template(Template value) {
    this.id = value.id;
    this.projectId = value.projectId;
    this.name = value.name;
    this.config = value.config;
    this.shipKey = value.shipKey;
    this.isDeleted = value.isDeleted;
    this.updatedAt = value.updatedAt;
  }

  public Template(
    UUID id,
    UUID projectId,
    String name,
    String config,
    String shipKey,
    Boolean isDeleted,
    Long updatedAt
  ) {
    this.id = id;
    this.projectId = projectId;
    this.name = name;
    this.config = config;
    this.shipKey = shipKey;
    this.isDeleted = isDeleted;
    this.updatedAt = updatedAt;
  }

  /**
   Getter for <code>xj.template.id</code>.
   */
  public UUID getId() {
    return this.id;
  }

  /**
   Setter for <code>xj.template.id</code>.
   */
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   Getter for <code>xj.template.project_id</code>.
   */
  public UUID getProjectId() {
    return this.projectId;
  }

  /**
   Setter for <code>xj.template.project_id</code>.
   */
  public void setProjectId(UUID projectId) {
    this.projectId = projectId;
  }

  /**
   Getter for <code>xj.template.name</code>.
   */
  public String getName() {
    return this.name;
  }

  /**
   Setter for <code>xj.template.name</code>.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   Getter for <code>xj.template.config</code>.
   */
  public String getConfig() {
    return this.config;
  }

  /**
   Setter for <code>xj.template.config</code>.
   */
  public void setConfig(String config) {
    this.config = config;
  }

  /**
   Getter for <code>xj.template.ship_key</code>.
   */
  public String getShipKey() {
    return this.shipKey;
  }

  /**
   Setter for <code>xj.template.ship_key</code>.
   */
  public void setShipKey(String shipKey) {
    this.shipKey = shipKey;
  }

  /**
   Getter for <code>xj.template.is_deleted</code>.
   */
  public Boolean getIsDeleted() {
    return this.isDeleted;
  }

  /**
   Setter for <code>xj.template.is_deleted</code>.
   */
  public void setIsDeleted(Boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

  /**
   Getter for <code>xj.template.updated_at</code>.
   */
  public Long getUpdatedAt() {
    return this.updatedAt;
  }

  /**
   Setter for <code>xj.template.updated_at</code>.
   */
  public void setUpdatedAt(Long updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Template (");

    sb.append(id);
    sb.append(", ").append(projectId);
    sb.append(", ").append(name);
    sb.append(", ").append(config);
    sb.append(", ").append(shipKey);
    sb.append(", ").append(isDeleted);
    sb.append(", ").append(updatedAt);

    sb.append(")");
    return sb.toString();
  }

  @Override
  public int compareTo(Template o) {
    if (!Objects.equals(name, o.name))
      return name.compareTo(o.name);
    return id.compareTo(o.id);

  }
}
