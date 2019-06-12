//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.entity.impl;

import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.entity.SubEntity;
import io.xj.core.model.entity.SuperEntity;
import io.xj.core.model.payload.Payload;
import io.xj.core.model.payload.PayloadObject;
import io.xj.core.util.Text;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 [#166690830] Program model handles all of its own entities
 <p>
 POJO for persisting data in memory AS json
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public abstract class SuperEntityImpl extends EntityImpl implements SuperEntity {
  final Logger log = LoggerFactory.getLogger(SuperEntityImpl.class);

  @Override
  public Entity setId(BigInteger id) {
    this.id = id;
    return this;
  }

  @Override
  public Entity setCreatedAt(String createdAt) {
    this.createdAt = Instant.parse(createdAt);
    return this;
  }

  @Override
  public Entity setCreatedAtInstant(Instant createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  @Override
  public Entity setUpdatedAt(String updatedAt) {
    this.updatedAt = Instant.parse(updatedAt);
    return this;
  }

  @Override
  public Entity setUpdatedAtInstant(Instant updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  /**
   Add any Sub-entity

   @param sub   to add
   @param <Sub> type
   @throws CoreException on failure
   */
  private <Sub extends SubEntity> void add(Sub sub) throws CoreException {
    Method method = ReflectionUtils.getAllMethods(getClass(),
      ReflectionUtils.withModifier(Modifier.PUBLIC),
      ReflectionUtils.withName("add"),
      ReflectionUtils.withParametersCount(1),
      ReflectionUtils.withParameters(sub.getClass())
    ).iterator().next();

    try {
      method.invoke(this, sub);
    } catch (IllegalAccessException e) {
      log.error("Failure to invoke method to add {} to {}", sub, this, e);
      throw new CoreException(String.format("Failure to invoke method to add %s", sub), e);
    } catch (InvocationTargetException e) {
      log.warn("Cannot add {}(id={}) to {}(id={}): {}", Text.getSimpleName(sub), sub.getId(), Text.getSimpleName(this), this.getId(), e.getTargetException().getMessage());
      throw new CoreException(e.getTargetException().getMessage());
    }
  }

  /**
   Synchronize Sub-entities with payload

   @param <Sub>    sub-entity type
   @param payload  with which to synchronize sub-entities
   @param subMap   of sub-entities
   @param subClass of sub-entities
   @throws CoreException on failure
   */
  protected <Sub extends SubEntity> void syncSubEntities(Payload payload, Map<UUID, Sub> subMap, Class<Sub> subClass) throws CoreException {
    PayloadObject superObj = extractPrimaryObject(payload, getResourceType());

    for (PayloadObject subObj : payload.getIncludedOfType(SubEntity.newInstance(subClass).getResourceType())) {
      Sub sub = SubEntity.newInstance(subClass);
      sub.consume(subObj);
      if (!subMap.containsKey(sub.getId())) {
        add(sub);
      }
    }

    Collection<String> subIds = superObj.getRelationshipDataMany(Text.toResourceHasMany(subClass)).stream()
      .map(PayloadObject::getId)
      .collect(Collectors.toList());
    Collection<UUID> orphanIds = subMap.keySet().stream()
      .filter(uuid -> !subIds.contains(uuid.toString()))
      .collect(Collectors.toList());
    orphanIds.forEach(subMap::remove);
  }

  /**
   Require that the super entity have an idea (i.e., before adding sub-entities)

   @param condition requiring id, e.g. "before adding sub-entities"
   @throws CoreException if no id is present
   */
  protected void requireId(String condition) throws CoreException {
    if (Objects.isNull(id))
      throw new CoreException(String.format("%s must have id %s", Text.getSimpleName(this), condition));
  }

}
