// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.ingest.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.DAO;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.dao.LibraryDAO;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.ingest.Ingest;
import io.xj.core.model.chain.sub.ChainBinding;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.library.Library;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.ProgramType;
import io.xj.core.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 [#154350346] Architect wants a universal Ingest Factory, to modularize graph mathematics used during craft to ingest any combination of Library, Sequence, and Instrument for any purpose.
 */
public class IngestImpl implements Ingest {
  private final Access access;
  private final Logger log = LoggerFactory.getLogger(IngestImpl.class);
  private final LibraryDAO libraryDAO;
  private final ProgramDAO programDAO;
  private final InstrumentDAO instrumentDAO;
  private final Collection<ChainBinding> bindings;
  private final Map<BigInteger, Library> libraryMap = Maps.newHashMap();
  private final Map<BigInteger, Program> programMap = Maps.newHashMap();
  private final Map<BigInteger, Instrument> instrumentMap = Maps.newHashMap();

  @Inject
  public IngestImpl(
    @Assisted("access") Access access,
    @Assisted("bindings") Collection<ChainBinding> bindings,
    LibraryDAO libraryDAO,
    ProgramDAO programDAO,
    InstrumentDAO instrumentDAO
  ) {
    this.access = access;
    this.libraryDAO = libraryDAO;
    this.bindings = Lists.newArrayList(bindings);
    this.instrumentDAO = instrumentDAO;
    this.programDAO = programDAO;
    readAll();
  }

  /**
   Fetch one entity from an entity map

   @param <E>       class of entity
   @param entityMap to fetch from as primary source
   @param entityId  to fetch
   @return entity
   */
  private static <E extends Entity> E fetchOne(Map<BigInteger, E> entityMap, BigInteger entityId) throws CoreException {
    if (entityMap.isEmpty()) {
      throw new CoreException(String.format("Cannot fetch one entity from empty %s map", entityMap.values().getClass()));
    }

    if (Objects.isNull(entityId)) {
      throw new CoreException(String.format("Cannot fetch null id from %s entity map", Text.getSimpleName(entityMap.values().iterator().next())));
    }

    if (!entityMap.containsKey(entityId)) {
      throw new CoreException(String.format("Cannot fetch entityId=%s from %s entity map", entityId, Text.getSimpleName(entityMap.values().iterator().next())));
    }

    return entityMap.get(entityId);
  }

  @Override
  public Collection<Program> getAllPrograms() {
    return programMap.values();
  }

  @Override
  public Collection<Program> getProgramsOfType(ProgramType type) {
    ImmutableList.Builder<Program> result = ImmutableList.builder();
    programMap.values().forEach(program -> {
      if (type == program.getType())
        result.add(program);
    });
    return result.build();
  }

  @Override
  public Access getAccess() {
    return access;
  }

  @Override
  public Program getProgram(BigInteger id) throws CoreException {
    return fetchOne(programMap, id);
  }

  @Override
  public Collection<Instrument> getAllInstruments() {
    return instrumentMap.values();
  }

  @Override
  public Collection<Instrument> getInstrumentsOfType(InstrumentType type) {
    ImmutableList.Builder<Instrument> result = ImmutableList.builder();
    instrumentMap.values().forEach(instrument -> {
      if (type == instrument.getType())
        result.add(instrument);
    });
    return result.build();
  }

  @Override
  public Instrument getInstrument(BigInteger id) throws CoreException {
    return fetchOne(instrumentMap, id);
  }

  @Override
  public Collection<Library> getAllLibraries() {
    return libraryMap.values();
  }

  @Override
  public Collection<Entity> getAllEntities() {
    ImmutableList.Builder<Entity> result = ImmutableList.builder();
    result.addAll(getAllLibraries());
    result.addAll(getAllPrograms());
    result.addAll(getAllInstruments());
    return result.build();
  }

  @Override
  public String toString() {
    return Entity.histogramString(getAllEntities());
  }

  /**
   Read all records via DAO
   for all entities and children
   NOTE: the order of operations inside here is important! hierarchical order, from parents to children
   */
  private void readAll() {
    try {
      readAll(Library.class, libraryMap, libraryDAO);
      readAll(Program.class, programMap, libraryMap.keySet(), programDAO);
      readAll(Instrument.class, instrumentMap, libraryMap.keySet(), instrumentDAO);
    } catch (Exception e) {
      log.error("Failed to read all entities for ingest.", e);
    }
  }

  /**
   Read all Entities and put results into map
   NOTE: the order of these operations is important! it is managed by the main readMany() process
   */
  private <E extends Entity> void readAll(Class<E> entityClass, Map<BigInteger, E> entityMap, Collection<BigInteger> parentIds, DAO<E> dao) throws CoreException {
    try {
      dao.readMany(access, parentIds).forEach(entity -> entityMap.put(entity.getId(), entity));

      for (BigInteger id : entityIds(entityMap, entityClass))
        if (!entityMap.containsKey(id))
          entityMap.put(id, dao.readOne(access, id));
    } catch (CoreException e) {
      throw new CoreException(String.format("Failed to retrieve entityMap=%s, parentIds=%s, dao=%s", entityMap, parentIds, dao.getClass().getInterfaces()[0].getName()), e);
    }
  }

  /**
   Read all Entities and put results into map
   NOTE: the order of these operations is important! it is managed by the main readMany() process
   */
  private <E extends Entity> void readAll(Class<E> entityClass, Map<BigInteger, E> entityMap, DAO<E> dao) throws CoreException {
    try {
      for (BigInteger id : entityIds(entityMap, entityClass))
        if (!entityMap.containsKey(id))
          entityMap.put(id, dao.readOne(access, id));
    } catch (CoreException e) {
      throw new CoreException(String.format("Failed to retrieve entityMap=%s, dao=%s", entityMap, dao.getClass().getInterfaces()[0].getName()), e);
    }
  }

  /**
   Get collection of Id, starting with a map of entity, and adding entity filtered by class from all source entities

   @param entityClass use only this class of source entities
   @return collection of id
   */
  private <E extends Entity> Collection<BigInteger> entityIds(Map<BigInteger, E> baseEntities, Class<E> entityClass) {
    Map<BigInteger, Boolean> result = Maps.newHashMap();
    baseEntities.forEach((id, entity) -> result.put(id, true));
    bindings.stream()
      .filter(match -> match.getTargetClass().equals(entityClass.getSimpleName()))
      .forEach(binding -> result.put(binding.getTargetId(), true));
    return result.keySet();
  }

}
