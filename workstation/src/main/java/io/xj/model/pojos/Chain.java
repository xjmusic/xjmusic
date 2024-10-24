// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.model.pojos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.xj.model.enums.ChainState;
import io.xj.model.enums.ChainType;

import java.util.Objects;
import java.util.UUID;


public class Chain implements Comparable<Chain> {
  @JsonIgnore
  public static final String EXTENSION_SEPARATOR = ".";
  UUID id;
  UUID projectId;
  UUID templateId;
  ChainType type;
  ChainState state;
  String shipKey;
  String templateConfig;
  String name;

  /**
   **/
  public Chain id(UUID id) {
    this.id = id;
    return this;
  }


  @JsonProperty("id")
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  /**
   **/
  public Chain projectId(UUID projectId) {
    this.projectId = projectId;
    return this;
  }


  @JsonProperty("projectId")
  public UUID getProjectId() {
    return projectId;
  }

  public void setProjectId(UUID projectId) {
    this.projectId = projectId;
  }

  /**
   **/
  public Chain templateId(UUID templateId) {
    this.templateId = templateId;
    return this;
  }


  @JsonProperty("templateId")
  public UUID getTemplateId() {
    return templateId;
  }

  public void setTemplateId(UUID templateId) {
    this.templateId = templateId;
  }

  /**
   **/
  public Chain type(ChainType type) {
    this.type = type;
    return this;
  }


  @JsonProperty("type")
  public ChainType getType() {
    return type;
  }

  public void setType(ChainType type) {
    this.type = type;
  }

  /**
   **/
  public Chain state(ChainState state) {
    this.state = state;
    return this;
  }


  @JsonProperty("state")
  public ChainState getState() {
    return state;
  }

  public void setState(ChainState state) {
    this.state = state;
  }

  /**
   **/
  public Chain shipKey(String shipKey) {
    this.shipKey = shipKey;
    return this;
  }


  @JsonProperty("shipKey")
  public String getShipKey() {
    return shipKey;
  }

  public void setShipKey(String shipKey) {
    this.shipKey = shipKey;
  }

  /**
   **/
  public Chain templateConfig(String templateConfig) {
    this.templateConfig = templateConfig;
    return this;
  }


  @JsonProperty("templateConfig")
  public String getTemplateConfig() {
    return templateConfig;
  }

  public void setTemplateConfig(String templateConfig) {
    this.templateConfig = templateConfig;
  }

  /**
   **/
  public Chain name(String name) {
    this.name = name;
    return this;
  }


  @JsonProperty("name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Chain chain = (Chain) o;
    return Objects.equals(this.id, chain.id) &&
      Objects.equals(this.projectId, chain.projectId) &&
      Objects.equals(this.templateId, chain.templateId) &&
      Objects.equals(this.type, chain.type) &&
      Objects.equals(this.state, chain.state) &&
      Objects.equals(this.shipKey, chain.shipKey) &&
      Objects.equals(this.templateConfig, chain.templateConfig) &&
      Objects.equals(this.name, chain.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, projectId, templateId, type, state, shipKey, templateConfig, name);
  }

  @Override
  public String toString() {

    return "class Chain {\n" +
      "    id: " + toIndentedString(id) + "\n" +
      "    projectId: " + toIndentedString(projectId) + "\n" +
      "    templateId: " + toIndentedString(templateId) + "\n" +
      "    type: " + toIndentedString(type) + "\n" +
      "    state: " + toIndentedString(state) + "\n" +
      "    shipKey: " + toIndentedString(shipKey) + "\n" +
      "    templateConfig: " + toIndentedString(templateConfig) + "\n" +
      "    name: " + toIndentedString(name) + "\n" +
      "}";
  }

  /**
   Convert the given object to string with each line indented by 4 spaces
   (except the first line).
   */
  String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

  @Override
  public int compareTo(Chain o) {
    if (!Objects.equals(name, o.name))
      return name.compareTo(o.name);
    return id.compareTo(o.id);
  }
}

