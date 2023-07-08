// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.Objects;
import java.util.UUID;


public class Chain {

  @Valid UUID id;
  @Valid UUID accountId;
  @Valid UUID templateId;
  @Valid ChainType type;
  @Valid ChainState state;
  @Valid String shipKey;
  @Valid String templateConfig;
  @Valid String name;

  /**
   *
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
   *
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
   *
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
   *
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
   *
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
   *
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
   *
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
   *
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
      Objects.equals(this.accountId, chain.accountId) &&
      Objects.equals(this.templateId, chain.templateId) &&
      Objects.equals(this.type, chain.type) &&
      Objects.equals(this.state, chain.state) &&
      Objects.equals(this.shipKey, chain.shipKey) &&
      Objects.equals(this.templateConfig, chain.templateConfig) &&
      Objects.equals(this.name, chain.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, accountId, templateId, type, state, shipKey, templateConfig, name);
  }

  @Override
  public String toString() {

    return "class Chain {\n" +
      "    id: " + toIndentedString(id) + "\n" +
      "    accountId: " + toIndentedString(accountId) + "\n" +
      "    templateId: " + toIndentedString(templateId) + "\n" +
      "    type: " + toIndentedString(type) + "\n" +
      "    state: " + toIndentedString(state) + "\n" +
      "    shipKey: " + toIndentedString(shipKey) + "\n" +
      "    templateConfig: " + toIndentedString(templateConfig) + "\n" +
      "    name: " + toIndentedString(name) + "\n" +
      "}";
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

