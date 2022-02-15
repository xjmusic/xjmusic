// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.inject.Inject;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.TemplateType;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplatePlayback;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static io.xj.hub.Tables.TEMPLATE;
import static io.xj.hub.Tables.TEMPLATE_PLAYBACK;

public class TemplatePlaybackManagerImpl extends HubPersistenceServiceImpl<TemplatePlayback> implements TemplatePlaybackManager {
  private final long playbackExpireSeconds;

  @Inject
  public TemplatePlaybackManagerImpl(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider,
    Environment env
  ) {
    super(entityFactory, dbProvider);

    playbackExpireSeconds = env.getPlaybackExpireSeconds();
  }

  @Override
  public TemplatePlayback create(HubAccess hubAccess, TemplatePlayback raw) throws ManagerException, JsonapiException, ValueException {
    DSLContext db = dbProvider.getDSL();
    raw.setUserId(hubAccess.getUserId());
    TemplatePlayback record = validate(raw);
    requireArtist(hubAccess);

    if (!hubAccess.isTopLevel())
      requireExists("Access to template", db.selectCount().from(TEMPLATE)
        .where(TEMPLATE.ID.eq(record.getTemplateId()))
        .and(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne(0, int.class));

    var template = modelFrom(Template.class, dbProvider.getDSL().selectFrom(TEMPLATE)
      .where(TEMPLATE.ID.eq(record.getTemplateId()))
      .fetchOne());
    requireAny("Preview-type Template", TemplateType.Preview.equals(template.getType()));

    for (var prior : modelsFrom(TemplatePlayback.class,
      db.selectFrom(TEMPLATE_PLAYBACK)
        .where(TEMPLATE_PLAYBACK.USER_ID.eq(record.getUserId()))
        .or(TEMPLATE_PLAYBACK.TEMPLATE_ID.eq(record.getTemplateId()))
        .fetch()))
      destroy(hubAccess, prior.getId());
    return modelFrom(TemplatePlayback.class, executeCreate(db, TEMPLATE_PLAYBACK, record));
  }

  @Override
  @Nullable
  public TemplatePlayback readOne(HubAccess hubAccess, UUID id) throws ManagerException {
    return readOne(dbProvider.getDSL(), hubAccess, id);
  }

  @Override
  public Optional<TemplatePlayback> readOneForUser(HubAccess hubAccess, UUID userId) throws ManagerException {
    DSLContext db = dbProvider.getDSL();
    var playbackRecord = hubAccess.isTopLevel()
      ?
      db.selectFrom(TEMPLATE_PLAYBACK)
        .where(TEMPLATE_PLAYBACK.USER_ID.eq(userId))
        .and(TEMPLATE_PLAYBACK.CREATED_AT.greaterThan(Timestamp.from(Instant.now().minusSeconds(playbackExpireSeconds)).toLocalDateTime()))
        .fetchOne()
      :
      db.select(TEMPLATE_PLAYBACK.fields())
        .from(TEMPLATE_PLAYBACK)
        .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_PLAYBACK.TEMPLATE_ID))
        .where(TEMPLATE_PLAYBACK.USER_ID.eq(userId))
        .and(TEMPLATE_PLAYBACK.CREATED_AT.greaterThan(Timestamp.from(Instant.now().minusSeconds(playbackExpireSeconds)).toLocalDateTime()))
        .and(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne();

    return Objects.nonNull(playbackRecord) ? Optional.of(modelFrom(TemplatePlayback.class, playbackRecord)) : Optional.empty();
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws ManagerException {
    DSLContext db = dbProvider.getDSL();

    if (!hubAccess.isTopLevel())
      requireExists("Access to the template's account",
        db.selectCount().from(TEMPLATE_PLAYBACK)
          .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_PLAYBACK.TEMPLATE_ID))
          .where(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetchOne(0, int.class));

    db.deleteFrom(TEMPLATE_PLAYBACK)
      .where(TEMPLATE_PLAYBACK.ID.eq(id))
      .execute();
  }

  @Override
  public TemplatePlayback newInstance() {
    return new TemplatePlayback();
  }

  @Override
  public Collection<TemplatePlayback> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws ManagerException {
    if (hubAccess.isTopLevel())
      return modelsFrom(TemplatePlayback.class, dbProvider.getDSL().select(TEMPLATE_PLAYBACK.fields())
        .from(TEMPLATE_PLAYBACK)
        .where(TEMPLATE_PLAYBACK.TEMPLATE_ID.in(parentIds))
        .and(TEMPLATE_PLAYBACK.CREATED_AT.greaterThan(Timestamp.from(Instant.now().minusSeconds(playbackExpireSeconds)).toLocalDateTime()))
        .orderBy(TEMPLATE_PLAYBACK.USER_ID)
        .fetch());
    else
      return modelsFrom(TemplatePlayback.class, dbProvider.getDSL().select(TEMPLATE_PLAYBACK.fields())
        .from(TEMPLATE_PLAYBACK)
        .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_PLAYBACK.TEMPLATE_ID))
        .where(TEMPLATE_PLAYBACK.TEMPLATE_ID.in(parentIds))
        .and(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .and(TEMPLATE_PLAYBACK.CREATED_AT.greaterThan(Timestamp.from(Instant.now().minusSeconds(playbackExpireSeconds)).toLocalDateTime()))
        .orderBy(TEMPLATE_PLAYBACK.USER_ID)
        .fetch());
  }

  @Override
  public TemplatePlayback update(HubAccess hubAccess, UUID id, TemplatePlayback rawTemplatePlayback) throws ManagerException, JsonapiException, ValueException {
    throw new ManagerException("Can't update a Template Playback");
  }

  /**
   Read one record

   @param db        DSL context
   @param hubAccess control
   @param id        to read
   @return record
   @throws ManagerException on failure
   */
  private TemplatePlayback readOne(DSLContext db, HubAccess hubAccess, UUID id) throws ManagerException {
    if (hubAccess.isTopLevel())
      return modelFrom(TemplatePlayback.class, db.selectFrom(TEMPLATE_PLAYBACK)
        .where(TEMPLATE_PLAYBACK.ID.eq(id))
        .fetchOne());
    else
      return modelFrom(TemplatePlayback.class, db.select(TEMPLATE_PLAYBACK.fields())
        .from(TEMPLATE_PLAYBACK)
        .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_PLAYBACK.TEMPLATE_ID))
        .where(TEMPLATE_PLAYBACK.ID.eq(id))
        .and(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne());
  }

  /**
   Validate data

   @param builder to validate
   @throws ManagerException if invalid
   */
  public TemplatePlayback validate(TemplatePlayback builder) throws ManagerException {
    try {
      Values.require(builder.getTemplateId(), "Template ID");
      Values.require(builder.getUserId(), "User ID");

      return builder;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

}
