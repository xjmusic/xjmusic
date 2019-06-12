//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain.sub;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.chain.impl.ChainSubEntity;
import io.xj.core.model.entity.Entity;
import io.xj.core.util.Text;

import java.math.BigInteger;
import java.util.Collection;

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
public class ChainBinding extends ChainSubEntity {
  public static final Collection<String> ALLOWED_TARGET_CLASSES = ImmutableList.of("Library", "Program", "Instrument");
  private String targetClass;
  private BigInteger targetId;

  /**
   Get a new ChainBinding for a given target

   @param entity to get ChainBinding for
   @return new ChainBinding
   */
  public static ChainBinding from(Entity entity) {
    ChainBinding chainBinding = new ChainBinding();
    chainBinding.setTarget(entity);
    return chainBinding;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("targetClass")
      .add("targetId")
      .build();
  }

  /**
   Get target class bound to chain

   @return target class
   */
  public String getTargetClass() {
    return targetClass;
  }

  /**
   Get target id bound to chain

   @return target id
   */
  public BigInteger getTargetId() {
    return targetId;
  }

  /**
   Set Chain id

   @param chainId to set
   @return this ChainBinding (for chaining setters)
   */
  public ChainBinding setChainId(BigInteger chainId) {
    super.setChainId(chainId);
    return this;
  }

  /**
   Set target to bind to Chain

   @param target to set
   @return this ChainBinding (for chaining setters)
   */
  public ChainBinding setTarget(Entity target) {
    setTargetId(target.getId());
    setTargetClass(Text.getSimpleName(target));
    return this;
  }

  /**
   Set target id to bind to Chain

   @param targetId to bind
   @return this Chain Binding (for chaining setters)
   */
  public ChainBinding setTargetId(BigInteger targetId) {
    this.targetId = targetId;
    return this;
  }

  /**
   Set target class to bind to Chain

   @param targetClass to bind
   @return this Chain Binding (for chaining setters)
   */
  public ChainBinding setTargetClass(String targetClass) {
    this.targetClass = targetClass;
    return this;
  }

  @Override
  public ChainBinding validate() throws CoreException {
    super.validate();
    require(targetClass, "Chain-bound target class", ALLOWED_TARGET_CLASSES);
    require(targetId, "Chain-bound target ID");
    return this;
  }
}
