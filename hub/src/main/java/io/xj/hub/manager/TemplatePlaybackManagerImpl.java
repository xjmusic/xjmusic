// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.inject.Inject;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.TemplateType;
import io.xj.hub.kubernetes.KubernetesAdmin;
import io.xj.hub.kubernetes.KubernetesException;
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
  private final KubernetesAdmin kubernetesAdmin;
  private final long playbackExpireSeconds;

  @Inject
  public TemplatePlaybackManagerImpl(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider,
    KubernetesAdmin kubernetesAdmin,
    Environment env
  ) {
    super(entityFactory, dbProvider);
    this.kubernetesAdmin = kubernetesAdmin;

    playbackExpireSeconds = env.getPlaybackExpireSeconds();
  }

  @Override
  public TemplatePlayback create(HubAccess access, TemplatePlayback raw) throws ManagerException, JsonapiException, ValueException {
    DSLContext db = dbProvider.getDSL();
    raw.setUserId(access.getUserId());
    TemplatePlayback record = validate(raw);
    requireArtist(access);

    if (!access.isTopLevel())
      requireExists("Access to template", db.selectCount().from(TEMPLATE)
        .where(TEMPLATE.ID.eq(record.getTemplateId()))
        .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
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
      _destroy(access, prior.getId());

    var created = modelFrom(TemplatePlayback.class, executeCreate(db, TEMPLATE_PLAYBACK, record));
    try {
      kubernetesAdmin.startPreviewNexus(access.getUserId(), template);
    } catch (KubernetesException e) {
      _destroy(access, created.getId());
      throw new ManagerException(e);
    }
    return created;
  }

  @Override
  @Nullable
  public TemplatePlayback readOne(HubAccess access, UUID id) throws ManagerException {
    return readOne(dbProvider.getDSL(), access, id);
  }

  @Override
  public Optional<TemplatePlayback> readOneForUser(HubAccess access, UUID userId) throws ManagerException {
    DSLContext db = dbProvider.getDSL();
    var playbackRecord = access.isTopLevel()
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
        .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne();

    return Objects.nonNull(playbackRecord) ? Optional.of(modelFrom(TemplatePlayback.class, playbackRecord)) : Optional.empty();
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    _destroy(access, id);

    try {
      kubernetesAdmin.stopPreviewNexus(access.getUserId());
    } catch (KubernetesException e) {
      throw new ManagerException(e);
    }
  }

  @Override
  public TemplatePlayback newInstance() {
    return new TemplatePlayback();
  }

  @Override
  public Collection<TemplatePlayback> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    if (access.isTopLevel())
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
        .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
        .and(TEMPLATE_PLAYBACK.CREATED_AT.greaterThan(Timestamp.from(Instant.now().minusSeconds(playbackExpireSeconds)).toLocalDateTime()))
        .orderBy(TEMPLATE_PLAYBACK.USER_ID)
        .fetch());
  }

  @Override
  public TemplatePlayback update(HubAccess access, UUID id, TemplatePlayback rawTemplatePlayback) throws ManagerException, JsonapiException, ValueException {
    throw new ManagerException("Can't update a Template Playback");
  }

  /**
   Inner destroy method to avoid running
   @param access control
   @param id of template playback
   @throws ManagerException on failure
   */
  private void _destroy(HubAccess access, UUID id) throws ManagerException {
    DSLContext db = dbProvider.getDSL();

    if (!access.isTopLevel())
      requireExists("Access to the template's account",
        db.selectCount().from(TEMPLATE_PLAYBACK)
          .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_PLAYBACK.TEMPLATE_ID))
          .where(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne(0, int.class));

    db.deleteFrom(TEMPLATE_PLAYBACK)
      .where(TEMPLATE_PLAYBACK.ID.eq(id))
      .execute();
  }

  /**
   Read one record

   @param db     DSL context
   @param access control
   @param id     to read
   @return record
   @throws ManagerException on failure
   */
  private TemplatePlayback readOne(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    if (access.isTopLevel())
      return modelFrom(TemplatePlayback.class, db.selectFrom(TEMPLATE_PLAYBACK)
        .where(TEMPLATE_PLAYBACK.ID.eq(id))
        .fetchOne());
    else
      return modelFrom(TemplatePlayback.class, db.select(TEMPLATE_PLAYBACK.fields())
        .from(TEMPLATE_PLAYBACK)
        .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_PLAYBACK.TEMPLATE_ID))
        .where(TEMPLATE_PLAYBACK.ID.eq(id))
        .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
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
