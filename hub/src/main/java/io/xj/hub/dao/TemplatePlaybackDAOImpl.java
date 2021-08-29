// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.inject.Inject;
import io.xj.api.TemplatePlayback;
import io.xj.api.TemplateType;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
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

public class TemplatePlaybackDAOImpl extends DAOImpl<TemplatePlayback> implements TemplatePlaybackDAO {

  @Inject
  public TemplatePlaybackDAOImpl(
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider,
    Environment env
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public TemplatePlayback create(HubAccess hubAccess, TemplatePlayback raw) throws DAOException, JsonapiException, ValueException {
    DSLContext db = dbProvider.getDSL();
    raw.setUserId(hubAccess.getUserId());
    TemplatePlayback record = validate(raw);
    requireArtist(hubAccess);

    if (hubAccess.isTopLevel())
      requireExists("preview-type Template", db.selectCount().from(TEMPLATE)
        .where(TEMPLATE.ID.eq(record.getTemplateId()))
        .and(TEMPLATE.TYPE.eq(TemplateType.PREVIEW.toString()))
        .fetchOne(0, int.class));
    else
      requireExists("preview-type Template", db.selectCount().from(TEMPLATE)
        .where(TEMPLATE.ID.eq(record.getTemplateId()))
        .and(TEMPLATE.TYPE.eq(TemplateType.PREVIEW.toString()))
        .and(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne(0, int.class));

    for (var prior : modelsFrom(TemplatePlayback.class,
      db.selectFrom(TEMPLATE_PLAYBACK)
        .where(TEMPLATE_PLAYBACK.USER_ID.eq(record.getUserId()))
        .fetch()))
      destroy(hubAccess, prior.getId());
    return modelFrom(TemplatePlayback.class, executeCreate(db, TEMPLATE_PLAYBACK, record));
  }

  @Override
  @Nullable
  public TemplatePlayback readOne(HubAccess hubAccess, UUID id) throws DAOException {
    return readOne(dbProvider.getDSL(), hubAccess, id);
  }

  @Override
  public Optional<TemplatePlayback> readOneForUser(HubAccess hubAccess, UUID userId) throws DAOException {
    DSLContext db = dbProvider.getDSL();
    var playback = hubAccess.isTopLevel()
      ?
      modelFrom(TemplatePlayback.class, db.selectFrom(TEMPLATE_PLAYBACK)
        .where(TEMPLATE_PLAYBACK.USER_ID.eq(userId))
        .fetchOne())
      :
      modelFrom(TemplatePlayback.class, db.select(TEMPLATE_PLAYBACK.fields())
        .from(TEMPLATE_PLAYBACK)
        .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_PLAYBACK.TEMPLATE_ID))
        .where(TEMPLATE_PLAYBACK.USER_ID.eq(userId))
        .and(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne());
    return Objects.nonNull(playback) ? Optional.of(playback) : Optional.empty();
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
    DSLContext db = dbProvider.getDSL();

    if (!hubAccess.isTopLevel())
      requireExists("TemplatePlayback belonging to you", db.selectCount().from(TEMPLATE_PLAYBACK)
        .where(TEMPLATE_PLAYBACK.USER_ID.eq(hubAccess.getUserId()))
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
  public Collection<TemplatePlayback> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
    if (hubAccess.isTopLevel())
      return modelsFrom(TemplatePlayback.class, dbProvider.getDSL().select(TEMPLATE_PLAYBACK.fields())
        .from(TEMPLATE_PLAYBACK)
        .where(TEMPLATE_PLAYBACK.TEMPLATE_ID.in(parentIds))
        .orderBy(TEMPLATE_PLAYBACK.USER_ID)
        .fetch());
    else
      return modelsFrom(TemplatePlayback.class, dbProvider.getDSL().select(TEMPLATE_PLAYBACK.fields())
        .from(TEMPLATE_PLAYBACK)
        .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_PLAYBACK.TEMPLATE_ID))
        .where(TEMPLATE_PLAYBACK.TEMPLATE_ID.in(parentIds))
        .and(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .orderBy(TEMPLATE_PLAYBACK.USER_ID)
        .fetch());
  }

  @Override
  public TemplatePlayback update(HubAccess hubAccess, UUID id, TemplatePlayback rawTemplatePlayback) throws DAOException, JsonapiException, ValueException {
    throw new DAOException("Can't update a Template Playback");
  }

  /**
   Read one record

   @param db        DSL context
   @param hubAccess control
   @param id        to read
   @return record
   @throws DAOException on failure
   */
  private TemplatePlayback readOne(DSLContext db, HubAccess hubAccess, UUID id) throws DAOException {
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
   @throws DAOException if invalid
   */
  public TemplatePlayback validate(TemplatePlayback builder) throws DAOException {
    try {
      Value.require(builder.getTemplateId(), "Template ID");
      Value.require(builder.getUserId(), "User ID");

      return builder;

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

}
