// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.TemplateType;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.service.PreviewNexusAdmin;
import io.xj.hub.service.ServiceException;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplatePlayback;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static io.xj.hub.Tables.TEMPLATE;
import static io.xj.hub.Tables.TEMPLATE_PLAYBACK;

@Service
public class TemplatePlaybackManagerImpl extends HubPersistenceServiceImpl implements TemplatePlaybackManager {
  private final Logger LOG = LoggerFactory.getLogger(TemplatePlaybackManagerImpl.class);
  private final PreviewNexusAdmin previewNexusAdmin;
  private final long playbackExpireSeconds;

  public TemplatePlaybackManagerImpl(
    AppEnvironment env,
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider,
    PreviewNexusAdmin previewNexusAdmin
  ) {
    super(entityFactory, sqlStoreProvider);
    this.previewNexusAdmin = previewNexusAdmin;

    playbackExpireSeconds = env.getPlaybackExpireSeconds();
  }

  @Override
  public TemplatePlayback create(HubAccess access, TemplatePlayback raw) throws ManagerException, JsonapiException, ValueException {
    DSLContext db = sqlStoreProvider.getDSL();
    raw.setUserId(access.getUserId());
    TemplatePlayback playbackData = validate(raw);
    requireArtist(access);

    if (!access.isTopLevel())
      try (var selectTemplatePlayback = db.selectCount()) {
        requireExists("Access to template", selectTemplatePlayback.from(TEMPLATE)
          .where(TEMPLATE.ID.eq(playbackData.getTemplateId()))
          .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne(0, int.class));
      } catch (Exception e) {
        throw new ManagerException(e);
      }

    Template template;
    try (var S = sqlStoreProvider.getDSL().selectFrom(TEMPLATE)) {
      template = modelFrom(Template.class, S
        .where(TEMPLATE.ID.eq(playbackData.getTemplateId()))
        .fetchOne());
    }
    requireAny("Preview-type Template", TemplateType.Preview.equals(template.getType()));

    for (var prior : readAllForUser(access))
      doDestroy(prior.getId());

    var playback = modelFrom(TemplatePlayback.class, executeCreate(db, TEMPLATE_PLAYBACK, playbackData));
    try {
      previewNexusAdmin.startPreviewNexus(template, playback);
    } catch (Exception e) {
      doDestroy(playback.getId());
      throw new ManagerException(e);
    }
    return playback;
  }

  @Override
  @Nullable
  public TemplatePlayback readOne(HubAccess access, UUID id) throws ManagerException {
    return readOne(sqlStoreProvider.getDSL(), access, id);
  }

  @Override
  public Optional<TemplatePlayback> readOneForUser(HubAccess access, UUID userId) throws ManagerException {
    DSLContext db = sqlStoreProvider.getDSL();
    Record record;
    if (access.isTopLevel())
      try (var selectTemplatePlayback = db.selectFrom(TEMPLATE_PLAYBACK)) {
        record = selectTemplatePlayback
          .where(TEMPLATE_PLAYBACK.USER_ID.eq(userId))
          .and(TEMPLATE_PLAYBACK.CREATED_AT.greaterThan(Timestamp.from(Instant.now().minusSeconds(playbackExpireSeconds)).toLocalDateTime()))
          .fetchOne();
      } catch (Exception e) {
        throw new ManagerException(e);
      }
    else
      try (var selectTemplatePlayback = db.select(TEMPLATE_PLAYBACK.fields());
           var joinTemplate = selectTemplatePlayback
             .from(TEMPLATE_PLAYBACK)
             .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_PLAYBACK.TEMPLATE_ID))) {
        record = joinTemplate
          .where(TEMPLATE_PLAYBACK.USER_ID.eq(userId))
          .and(TEMPLATE_PLAYBACK.CREATED_AT.greaterThan(Timestamp.from(Instant.now().minusSeconds(playbackExpireSeconds)).toLocalDateTime()))
          .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne();
      } catch (Exception e) {
        throw new ManagerException(e);
      }
    return Objects.nonNull(record) ? Optional.of(modelFrom(TemplatePlayback.class, record)) : Optional.empty();
  }

  @Override
  public Optional<TemplatePlayback> readOneForTemplate(HubAccess access, UUID templateId) throws ManagerException {
    DSLContext db = sqlStoreProvider.getDSL();

    Record record;
    if (access.isTopLevel())
      try (var selectTemplatePlayback = db.selectFrom(TEMPLATE_PLAYBACK)) {
        record =
          selectTemplatePlayback
            .where(TEMPLATE_PLAYBACK.TEMPLATE_ID.eq(templateId))
            .and(TEMPLATE_PLAYBACK.CREATED_AT.greaterThan(Timestamp.from(Instant.now().minusSeconds(playbackExpireSeconds)).toLocalDateTime()))
            .fetchOne();
      } catch (Exception e) {
        throw new ManagerException(e);
      }
    else
      try (var selectTemplatePlayback = db.select(TEMPLATE_PLAYBACK.fields());
           var joinTemplate = selectTemplatePlayback
             .from(TEMPLATE_PLAYBACK)
             .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_PLAYBACK.TEMPLATE_ID))) {
        record =
          joinTemplate
            .where(TEMPLATE_PLAYBACK.TEMPLATE_ID.eq(templateId))
            .and(TEMPLATE_PLAYBACK.CREATED_AT.greaterThan(Timestamp.from(Instant.now().minusSeconds(playbackExpireSeconds)).toLocalDateTime()))
            .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
            .fetchOne();
      } catch (Exception e) {
        throw new ManagerException(e);
      }

    return Objects.nonNull(record) ? Optional.of(modelFrom(TemplatePlayback.class, record)) : Optional.empty();
  }

  @Override
  public String readPreviewNexusLog(HubAccess access, TemplatePlayback playback) {
    try {
      return previewNexusAdmin.getPreviewNexusLogs(playback);

    } catch (ServiceException e) {
      LOG.error("Service administrator failed to read logs for TemplatePlayback[{}]", playback.getId(), e);
      return String.format("Service administrator failed to read logs for TemplatePlayback[%s]: %s", playback.getId(), e.getMessage());
    }
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    var playback = readOne(access, id);
    if (Objects.isNull(playback))
      throw new ManagerException(String.format("TemplatePlayback[%s] does not exist!", id));

    doDestroy(playback.getId());

    try {
      previewNexusAdmin.stopPreviewNexus(playback);
    } catch (ServiceException e) {
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
      try (var selectTemplatePlayback = sqlStoreProvider.getDSL().select(TEMPLATE_PLAYBACK.fields())) {
        return modelsFrom(TemplatePlayback.class, selectTemplatePlayback
          .from(TEMPLATE_PLAYBACK)
          .where(TEMPLATE_PLAYBACK.TEMPLATE_ID.in(parentIds))
          .and(TEMPLATE_PLAYBACK.CREATED_AT.greaterThan(Timestamp.from(Instant.now().minusSeconds(playbackExpireSeconds)).toLocalDateTime()))
          .orderBy(TEMPLATE_PLAYBACK.USER_ID)
          .fetch());
      } catch (Exception e) {
        throw new ManagerException(e);
      }
    else
      try (var selectTemplatePlayback = sqlStoreProvider.getDSL().select(TEMPLATE_PLAYBACK.fields());
           var joinTemplate = selectTemplatePlayback
             .from(TEMPLATE_PLAYBACK)
             .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_PLAYBACK.TEMPLATE_ID))) {
        return modelsFrom(TemplatePlayback.class, joinTemplate
          .where(TEMPLATE_PLAYBACK.TEMPLATE_ID.in(parentIds))
          .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
          .and(TEMPLATE_PLAYBACK.CREATED_AT.greaterThan(Timestamp.from(Instant.now().minusSeconds(playbackExpireSeconds)).toLocalDateTime()))
          .orderBy(TEMPLATE_PLAYBACK.USER_ID)
          .fetch());
      } catch (Exception e) {
        throw new ManagerException(e);
      }
  }

  @Override
  public TemplatePlayback update(HubAccess access, UUID id, TemplatePlayback rawTemplatePlayback) throws ManagerException, JsonapiException, ValueException {
    throw new ManagerException("Can't update a Template Playback");
  }

  /**
   * Inner read all method
   *
   * @param access control to source user for which to retrieve template playbacks
   * @return list of template playbacks
   * @throws ManagerException on failure
   */
  private Collection<TemplatePlayback> readAllForUser(HubAccess access) throws ManagerException {
    DSLContext db = sqlStoreProvider.getDSL();

    try (var selectTemplatePlayback = db.selectFrom(TEMPLATE_PLAYBACK)) {
      return modelsFrom(TemplatePlayback.class,
        selectTemplatePlayback
          .where(TEMPLATE_PLAYBACK.USER_ID.eq(access.getUserId()))
          .fetch());
    } catch (Exception e) {
      throw new ManagerException(e);
    }
  }

  /**
   * Inner destroy method to avoid replication of code
   *
   * @param templatePlaybackId of template playback
   * @throws ManagerException on failure
   */
  private void doDestroy(UUID templatePlaybackId) throws ManagerException {
    DSLContext db = sqlStoreProvider.getDSL();

    try (var deleteTemplatePlayback = db.deleteFrom(TEMPLATE_PLAYBACK)) {
      deleteTemplatePlayback
        .where(TEMPLATE_PLAYBACK.ID.eq(templatePlaybackId))
        .execute();
    } catch (Exception e) {
      throw new ManagerException(e);
    }
  }

  /**
   * Read one record
   *
   * @param db     DSL context
   * @param access control
   * @param id     to read
   * @return record
   */
  private @Nullable TemplatePlayback readOne(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    @Nullable Record playback;
    if (access.isTopLevel())
      try (var selectTemplatePlayback = db.selectFrom(TEMPLATE_PLAYBACK)) {
        playback = selectTemplatePlayback
          .where(TEMPLATE_PLAYBACK.ID.eq(id))
          .and(TEMPLATE_PLAYBACK.CREATED_AT.greaterThan(Timestamp.from(Instant.now().minusSeconds(playbackExpireSeconds)).toLocalDateTime()))
          .fetchOne();
      }
    else
      try (var selectTemplatePlayback = db.select(TEMPLATE_PLAYBACK.fields());
           var joinTemplate = selectTemplatePlayback
             .from(TEMPLATE_PLAYBACK)
             .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_PLAYBACK.TEMPLATE_ID))) {
        playback = joinTemplate
          .where(TEMPLATE_PLAYBACK.ID.eq(id))
          .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
          .and(TEMPLATE_PLAYBACK.CREATED_AT.greaterThan(Timestamp.from(Instant.now().minusSeconds(playbackExpireSeconds)).toLocalDateTime()))
          .fetchOne();
      }
    return Objects.nonNull(playback) ? modelFrom(TemplatePlayback.class, playback) : null;
  }

  /**
   * Validate data
   *
   * @param builder to validate
   * @throws ManagerException if invalid
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
