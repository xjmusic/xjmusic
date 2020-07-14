// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.entity;

import io.xj.lib.entity.Entity;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;

import java.util.UUID;

/**
 [#160980748] Developer wants all chain binding models to extend `ChainBinding` with common properties and methods pertaining to Chain membership.
 <p>
 POJO for persisting data in memory while performing business logic,
 or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ChainBinding extends Entity {


  private ChainBindingType type;
  private UUID targetId;
  private UUID chainId;
  private ValueException typeException;

  /**
   Get a ChainBinding

   @param chain  to bind
   @param target to bind
   @return new chain binding
   @throws ValueException on failure to of binding
   */
  public static <N extends Entity> ChainBinding create(Chain chain, N target) throws ValueException {
    return create()
      .setChainId(chain.getId())
      .setTarget(target);
  }

  /**
   Get a ChainBinding

   @return new chain binding
   */
  public static ChainBinding create() {
    return (ChainBinding) new ChainBinding().setId(UUID.randomUUID());
  }

  /**
   Get id of Chain to which this entity belongs

   @return chain id
   */
  public UUID getChainId() {
    return chainId;
  }

  /**
   Set id of Chain to which this entity belongs

   @param chainId to which this entity belongs
   @return this Chain Entity (for chaining setters)
   */
  public ChainBinding setChainId(UUID chainId) {
    this.chainId = chainId;
    return this;
  }

  /**
   Get type

   @return type
   */
  public ChainBindingType getType() {
    return type;
  }

  /**
   Get target id bound to chain

   @return target id
   */
  public UUID getTargetId() {
    return targetId;
  }

  /**
   Set target to bind to Chain

   @param target to set
   @return this ChainBinding (for chaining setters)
   */
  public ChainBinding setTarget(Entity target) throws ValueException {
    setTargetId(target.getId());
    setTypeEnum(ChainBindingType.valueOf(target));
    return this;
  }

  /**
   Set target id to bind to Chain

   @param targetId to bind
   @return this Chain Binding (for chaining setters)
   */
  public ChainBinding setTargetId(UUID targetId) {
    this.targetId = targetId;
    return this;
  }

  /**
   @param typeString pending validation
   */
  public ChainBinding setType(String typeString) {
    try {
      type = ChainBindingType.validate(typeString);
    } catch (ValueException e) {
      typeException = e;
    }
    return this;
  }

  /**
   Set type enum

   @param type enum to set
   @return self
   */
  public ChainBinding setTypeEnum(ChainBindingType type) {
    this.type = type;
    return this;
  }

  @Override
  public void validate() throws ValueException {
    super.validate();
    Value.require(chainId, "Chain ID");
    Value.require(targetId, "Chain-bound target ID");

    Value.requireNo(typeException, "Type");
    Value.require(type, "Type");
  }
}
