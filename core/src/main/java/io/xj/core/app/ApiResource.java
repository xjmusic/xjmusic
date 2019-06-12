// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.app;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.DAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.payload.Payload;
import io.xj.core.transport.GsonProvider;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.core.work.WorkManager;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class ApiResource {
  public Injector injector;
  public HttpResponseProvider response;
  public GsonProvider gsonProvider;
  public WorkManager workManager;

  /**
   Default constructor with no injector override
   <p>
   FUTURE: to unit test a hub resource, it must override both this default constructor and the injector-injecting constructor
   */
  public ApiResource() {
    injector = Guice.createInjector(new CoreModule());
    injectFields();
  }

  /**
   Inject fields shared by resources
   */
  private void injectFields() {
    response = injector.getInstance(HttpResponseProvider.class);
    gsonProvider = injector.getInstance(GsonProvider.class);
    workManager = injector.getInstance(WorkManager.class);
  }

  /**
   Set the injector (for unit testing)

   @param injector to inject
   */
  public void setInjector(Injector injector) {
    this.injector = injector;
    injectFields();
  }

  /**
   Create one Entity via a DAO given a JSON:API payload request

   @param crc     request context
   @param dao     via which to create Entity
   @param payload of data to create Entity
   @param <N>     type of Entity
   @return HTTP response comprising JSON:API payload
   */
  public <N extends Entity> Response create(ContainerRequestContext crc, DAO<N> dao, Payload payload) {
    try {
      Access access = Access.fromContext(crc);
      N createdEntity = dao.create(access, dao.newInstance().consume(payload));

      Payload responseData = new Payload();
      responseData.setDataOne(createdEntity.toPayloadObject());
      return response.create(responseData);

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

  /**
   Read one Entity from a DAO and return the JSON:API payload response

   @param crc request context
   @param dao from which to read one Entity
   @param id  of Entity to read
   @param <N> type of Entity
   @return HTTP response comprising JSON:API payload
   */
  public <N extends Entity> Response readOne(ContainerRequestContext crc, DAO<N> dao, String id) {
    try {
      Entity entity = dao.readOne(Access.fromContext(crc), new BigInteger(id));
      Payload payload = new Payload();
      payload.setDataEntity(entity);
      return response.ok(payload);

    } catch (CoreException ignored) {
      return response.notFound(dao.newInstance().setId(new BigInteger(id)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Read many Entity from a DAO and return the JSON:API payload response

   @param crc       request context
   @param dao       from which to read many Entity
   @param parentIds of Entity to read
   @param <N>       type of Entity
   @return HTTP response comprising JSON:API payload
   */
  public <N extends Entity> Response readMany(ContainerRequestContext crc, DAO<N> dao, Collection<String> parentIds) {
    try {
      Collection<N> entities = dao.readMany(Access.fromContext(crc), parentIds.stream().map(BigInteger::new).collect(Collectors.toList()));
      Payload payload = new Payload();
      payload.setDataEntities(entities, false);
      return response.ok(payload);

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Read many Entity from a DAO and return the JSON:API payload response

   @param crc      request context
   @param dao      from which to read many Entity
   @param parentId of Entity to read
   @param <N>      type of Entity
   @return HTTP response comprising JSON:API payload
   */
  public <N extends Entity> Response readMany(ContainerRequestContext crc, DAO<N> dao, String parentId) {
    if (Objects.isNull(parentId))
      return response.notAcceptable("parent id is required");

    return readMany(crc, dao, ImmutableList.of(parentId));
  }

  /**
   Update one Entity via a DAO given a JSON:API payload request

   @param crc     request context
   @param dao     via which to read one Entity
   @param id      of Entity to read
   @param payload of data to update
   @param <N>     type of Entity
   @return HTTP response comprising JSON:API payload
   */
  public <N extends Entity> Response update(ContainerRequestContext crc, DAO<N> dao, String id, Payload payload) {
    try {
      Access access = Access.fromContext(crc);
      N current = dao.readOne(access, new BigInteger(id));
      current.consume(payload);
      dao.update(access, new BigInteger(id), current);
      return response.ok(new Payload().setDataEntity(current));

    } catch (Exception e) {
      return response.failureToUpdate(e);
    }
  }

  /**
   Delete one Entity via a DAO

   @param crc request context
   @param dao via which to delete one Entity
   @param id  of Entity to delete
   @param <N> type of Entity
   @return HTTP response comprising JSON:API payload
   */
  public <N extends Entity> Response delete(ContainerRequestContext crc, DAO<N> dao, String id) {
    try {
      dao.destroy(Access.fromContext(crc), new BigInteger(id));
      return response.noContent();

    } catch (Exception e) {
      return response.failure(e);
    }
  }
}
