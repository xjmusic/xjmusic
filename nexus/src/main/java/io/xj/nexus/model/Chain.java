// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.Objects;
import java.util.UUID;


public class Chain   {

  private @Valid UUID id;
  private @Valid UUID accountId;
  private @Valid UUID templateId;
  private @Valid ChainType type;
  private @Valid ChainState state;
  private @Valid String shipKey;
  private @Valid String templateConfig;
  private @Valid String name;
  private @Valid String startAt;
  private @Valid String stopAt;
  private @Valid String fabricatedAheadAt;
  private @Valid String createdAt;
  private @Valid String updatedAt;

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
  public Chain accountId(UUID accountId) {
    this.accountId = accountId;
    return this;
  }


  @JsonProperty("accountId")
  public UUID getAccountId() {
    return accountId;
  }
  public void setAccountId(UUID accountId) {
    this.accountId = accountId;
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

  /**
   **/
  public Chain startAt(String startAt) {
    this.startAt = startAt;
    return this;
  }


  @JsonProperty("startAt")
  public String getStartAt() {
    return startAt;
  }
  public void setStartAt(String startAt) {
    this.startAt = startAt;
  }

  /**
   **/
  public Chain stopAt(String stopAt) {
    this.stopAt = stopAt;
    return this;
  }


  @JsonProperty("stopAt")
  public String getStopAt() {
    return stopAt;
  }
  public void setStopAt(String stopAt) {
    this.stopAt = stopAt;
  }

  /**
   **/
  public Chain fabricatedAheadAt(String fabricatedAheadAt) {
    this.fabricatedAheadAt = fabricatedAheadAt;
    return this;
  }


  @JsonProperty("fabricatedAheadAt")
  public String getFabricatedAheadAt() {
    return fabricatedAheadAt;
  }
  public void setFabricatedAheadAt(String fabricatedAheadAt) {
    this.fabricatedAheadAt = fabricatedAheadAt;
  }

  /**
   **/
  public Chain createdAt(String createdAt) {
    this.createdAt = createdAt;
    return this;
  }


  @JsonProperty("createdAt")
  public String getCreatedAt() {
    return createdAt;
  }
  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  /**
   **/
  public Chain updatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }


  @JsonProperty("updatedAt")
  public String getUpdatedAt() {
    return updatedAt;
  }
  public void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
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
        Objects.equals(this.accountId, chain.accountId) &&
        Objects.equals(this.templateId, chain.templateId) &&
        Objects.equals(this.type, chain.type) &&
        Objects.equals(this.state, chain.state) &&
        Objects.equals(this.shipKey, chain.shipKey) &&
        Objects.equals(this.templateConfig, chain.templateConfig) &&
        Objects.equals(this.name, chain.name) &&
        Objects.equals(this.startAt, chain.startAt) &&
        Objects.equals(this.stopAt, chain.stopAt) &&
        Objects.equals(this.fabricatedAheadAt, chain.fabricatedAheadAt) &&
        Objects.equals(this.createdAt, chain.createdAt) &&
        Objects.equals(this.updatedAt, chain.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, accountId, templateId, type, state, shipKey, templateConfig, name, startAt, stopAt, fabricatedAheadAt, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Chain {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    accountId: ").append(toIndentedString(accountId)).append("\n");
    sb.append("    templateId: ").append(toIndentedString(templateId)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    shipKey: ").append(toIndentedString(shipKey)).append("\n");
    sb.append("    templateConfig: ").append(toIndentedString(templateConfig)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    startAt: ").append(toIndentedString(startAt)).append("\n");
    sb.append("    stopAt: ").append(toIndentedString(stopAt)).append("\n");
    sb.append("    fabricatedAheadAt: ").append(toIndentedString(fabricatedAheadAt)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

