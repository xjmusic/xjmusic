// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableRecord;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 Utility to do a bunch of cloning,
 that keeps an inner UUID -> UUID map of all original ids to cloned ids
 and then swaps out all parent ids for the cloned parent ids
 of each successive cloning.
 */
public class DAOCloner<E> {
  private final E clone;
  private final DAO<?> anyDao;
  private final Collection<Object> childClones = Lists.newArrayList();
  private final Collection<DAOException> warnings = Lists.newArrayList();
  private final Map<UUID, UUID> clonedIds = Maps.newConcurrentMap();

  /**
   Instantiates a DAOCloner with a newly created entity,
   presumably the clone of an entity whose children
   will then be created using this DAOCloner.

   @param clone newly created entity whose children will be created using this DAOCloner.
   */
  DAOCloner(E clone, DAO<?> anyDao) {
    this.clone = clone;
    this.anyDao = anyDao;
  }

  /**
   Get warnings generated during cloning

   @return warnings
   */
  public Collection<DAOException> getWarnings() {
    return warnings;
  }

  /**
   Clone all records with a specified parent id to a new parent id,
   for each of the belongs-to relationships, if it belongs to a cloned id, replace the value with the cloned belongs-to id
   and return a set of the new cloned
   <p>
   SO successive calls to clone() can rely upon the replacement of relationship ids
   of all entities previously generated by a call to clone() on this Cloner instance@param db            DSL context

   @param table         in which to clone records (rows)
   @param idField       id column
   @param parentIdField parent id column
   @param fromParentId  to match records with
   @param toParentId    to of new records
   @return map of UUID originals -> clones
   */
  public <R extends TableRecord<?>> Map<UUID, UUID> clone(
    DSLContext db,
    Table<R> table,
    TableField<R, UUID> idField,
    Collection<TableField<R, UUID>> belongsToIdFields,
    TableField<R, UUID> parentIdField,
    UUID fromParentId,
    UUID toParentId
  ) throws DAOException {
    Collection<R> toInsert = Lists.newArrayList();
    Map<UUID, UUID> newlyClonedIds = Maps.newConcurrentMap();
    db.selectFrom(table)
      .where(parentIdField.eq(fromParentId))
      .fetch()
      .forEach(record -> {
        UUID originalId = record.get(idField);
        UUID clonedId = UUID.randomUUID();
        clonedIds.put(originalId, clonedId);
        newlyClonedIds.put(originalId, clonedId);

        // for each of the belongs-to relationships, if it belongs to a cloned id, replace the value with the cloned belongs-to id
        belongsToIdFields.forEach(belongsToIdField -> {
          if (Objects.nonNull(record.get(belongsToIdField))
            && clonedIds.containsKey(record.get(belongsToIdField)))
            record.set(belongsToIdField, clonedIds.get(record.get(belongsToIdField)));
        });
        record.set(idField, clonedId);
        record.set(parentIdField, toParentId);
        toInsert.add(record);
      });

    int[] rows = db.batchInsert(toInsert).execute();
    if (rows.length != toInsert.size())
      throw new DAOException(String.format("Only created %d out create %d intended %s records", rows.length, toInsert.size(), table.getName()));

    toInsert.forEach(record -> {
      try {
        childClones.add(anyDao.modelFrom(record));
      } catch (DAOException e) {
        warnings.add(e);
      }
    });

    return newlyClonedIds;
  }

  /**
   Get the cloned (parent) entity whose children were created using this DAOCloner

   @return cloned (parent) entity
   */
  public E getClone() {
    return clone;
  }

  /**
   Get all the children that were created using this DAOCloner

   @return collection of created child clone entities
   */
  public Collection<Object> getChildClones() {
    return childClones;
  }
}
