// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import java.util.Set;
import io.xj.hub.TemplateConfig;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.TemplateType;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.hub.tables.pojos.TemplatePlayback;
import io.xj.hub.tables.pojos.TemplatePublication;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.StringUtils;
import io.xj.lib.util.TremendouslyRandom;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.ValueUtils;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.hub.Tables.TEMPLATE;
import static io.xj.hub.Tables.TEMPLATE_BINDING;
import static io.xj.hub.Tables.TEMPLATE_PLAYBACK;
import static io.xj.hub.tables.Account.ACCOUNT;

@Service
public class TemplateManagerImpl extends HubPersistenceServiceImpl implements TemplateManager {
  static final int GENERATED_SHIP_KEY_LENGTH = 9;
  final long playbackExpireSeconds;
  final TemplateBindingManager templateBindingManager;
  final TemplatePlaybackManager templatePlaybackManager;
  final TemplatePublicationManager templatePublicationManager;

  @Autowired
  public TemplateManagerImpl(
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider,
    TemplateBindingManager templateBindingManager,
    TemplatePlaybackManager templatePlaybackManager,
    TemplatePublicationManager templatePublicationManager,
    @Value("${playback.expire.seconds}")
    long playbackExpireSeconds
  ) {
    super(entityFactory, sqlStoreProvider);
    this.templateBindingManager = templateBindingManager;
    this.templatePlaybackManager = templatePlaybackManager;
    this.templatePublicationManager = templatePublicationManager;
    this.playbackExpireSeconds = playbackExpireSeconds;
  }

  @Override
  public Template create(HubAccess access, Template entity) throws ManagerException, JsonapiException, ValueException {
    Template record = validate(access, entity);

    var db = sqlStoreProvider.getDSL();

    if (!access.isTopLevel())
      try (var selectCount = db.selectCount()) {
        requireExists("Account",
          selectCount.from(ACCOUNT)
            .where(ACCOUNT.ID.in(access.getAccountIds()))
            .fetchOne(0, int.class));
      } catch (Exception e) {
        throw new ManagerException(e);
      }

    try (var selectCount = db.selectCount()) {
      requireNotExists("Template with same Ship key",
        selectCount.from(TEMPLATE)
          .where(TEMPLATE.SHIP_KEY.eq(entity.getShipKey()))
          .and(TEMPLATE.IS_DELETED.eq(false))
          .fetchOne(0, int.class));
    } catch (Exception e) {
      throw new ManagerException(e);
    }

    return modelFrom(Template.class, executeCreate(sqlStoreProvider.getDSL(), TEMPLATE, record));
  }

  @Override
  public Template readOne(HubAccess access, UUID id) throws ManagerException {
    return readOne(sqlStoreProvider.getDSL(), access, id);
  }

  @Override
  public ManagerCloner<Template> clone(HubAccess access, UUID rawCloneId, Template to) throws ManagerException {
    requireArtist(access);
    AtomicReference<Template> result = new AtomicReference<>();
    AtomicReference<ManagerCloner<Template>> cloner = new AtomicReference<>();
    sqlStoreProvider.getDSL().transaction(ctx -> {
      DSLContext db = DSL.using(ctx);

      Template from = readOne(db, access, rawCloneId);
      if (Objects.isNull(from))
        throw new ManagerException("Can't clone nonexistent Template");

      // Lab cloned template is always Preview-type and has new ship key if unspecified https://www.pivotaltracker.com/story/show/181054239
      to.setType(TemplateType.Preview);
      if (StringUtils.isNullOrEmpty(to.getShipKey()))
        to.setShipKey(StringUtils.incrementIntegerSuffix(from.getShipKey()));

      // Inherits state, type if none specified
      // When not set, clone inherits attribute values from original record
      entityFactory.setAllEmptyAttributes(from, to);

      // Validate template
      Template template = validate(access, to);
      requireParentExists(db, access, template);

      // Create main entity
      result.set(modelFrom(Template.class, executeCreate(db, TEMPLATE, template)));
      UUID originalId = result.get().getId();

      // Prepare to clone sub-entities
      cloner.set(new ManagerCloner<>(result.get(), this));

      // DON'T clone TemplatePlayback- they are ephemeral, an indicator of playback state, non-existent initially

      // Clone TemplateBinding
      cloner.get().clone(db, TEMPLATE_BINDING, TEMPLATE_BINDING.ID, Set.of(), TEMPLATE_BINDING.TEMPLATE_ID, rawCloneId, originalId);
    });
    return cloner.get();
  }

  @Override
  public Optional<Template> readOneByShipKey(HubAccess access, String rawShipKey) throws ManagerException {
    String key = StringUtils.toShipKey(rawShipKey);
    if (access.isTopLevel())
      try (var selectFromTemplate = sqlStoreProvider.getDSL().selectFrom(TEMPLATE)) {
        return Optional.ofNullable(modelFrom(Template.class, selectFromTemplate
          .where(TEMPLATE.SHIP_KEY.eq(key))
          .and(TEMPLATE.IS_DELETED.eq(false))
          .fetchOne()));
      } catch (Exception e) {
        throw new ManagerException(e);
      }
    else
      try (var selectTemplate = sqlStoreProvider.getDSL().select(TEMPLATE.fields())) {
        return Optional.ofNullable(modelFrom(Template.class,
          selectTemplate
            .from(TEMPLATE)
            .where(TEMPLATE.SHIP_KEY.eq(key))
            .and(TEMPLATE.IS_DELETED.eq(false))
            .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
            .fetchOne()));
      } catch (Exception e) {
        throw new ManagerException(e);
      }
  }

  @Override
  public Collection<Template> readAllPlaying(HubAccess access) throws ManagerException {
    requireTopLevel(access);
    DSLContext db = sqlStoreProvider.getDSL();

    try (var selectTemplate = db.select(TEMPLATE.fields());
         var joinTemplatePlayback = selectTemplate
           .from(TEMPLATE)
           .join(TEMPLATE_PLAYBACK).on(TEMPLATE.ID.eq(TEMPLATE_PLAYBACK.TEMPLATE_ID))) {
      return modelsFrom(Template.class,
        joinTemplatePlayback
          .where(TEMPLATE_PLAYBACK.CREATED_AT.greaterThan(Timestamp.from(Instant.now().minusSeconds(playbackExpireSeconds)).toLocalDateTime()))
          .and(TEMPLATE.IS_DELETED.eq(false))
          .fetch());
    } catch (Exception e) {
      throw new ManagerException(e);
    }
  }

  @Override
  public Collection<Object> readChildEntities(HubAccess access, Collection<UUID> templateIds, Collection<String> includeTypes) throws ManagerException {
    DSLContext db = sqlStoreProvider.getDSL();

    requireRead(db, access, templateIds);

    Collection<Object> entities = new ArrayList<>();

    // TemplateBinding
    if (includeTypes.contains(Entities.toResourceType(TemplateBinding.class)))
      entities.addAll(templateBindingManager.readMany(access, templateIds));

    // TemplatePlayback
    if (includeTypes.contains(Entities.toResourceType(TemplatePlayback.class)))
      entities.addAll(templatePlaybackManager.readMany(access, templateIds));

    // TemplatePublication
    if (includeTypes.contains(Entities.toResourceType(TemplatePublication.class)))
      entities.addAll(templatePublicationManager.readMany(access, templateIds));

    return entities;
  }

  @Override
  public Optional<Template> readOnePlayingForUser(HubAccess access, UUID userId) throws ManagerException {
    requireTopLevel(access);
    DSLContext db = sqlStoreProvider.getDSL();

    try (var selectTemplate = db.select(TEMPLATE.fields());
         var joinTemplatePlayback = selectTemplate.from(TEMPLATE)
           .join(TEMPLATE_PLAYBACK).on(TEMPLATE.ID.eq(TEMPLATE_PLAYBACK.TEMPLATE_ID))) {
      var record =
        joinTemplatePlayback
          .where(TEMPLATE_PLAYBACK.CREATED_AT.greaterThan(Timestamp.from(Instant.now().minusSeconds(playbackExpireSeconds)).toLocalDateTime()))
          .and(TEMPLATE_PLAYBACK.USER_ID.eq(userId))
          .and(TEMPLATE.IS_DELETED.eq(false))
          .fetchOne();
      if (Objects.isNull(record)) return Optional.empty();
      return Optional.of(modelFrom(Template.class, record));
    } catch (Exception e) {
      throw new ManagerException(e);
    }
  }

  @Override
  public String readPreviewNexusLog(HubAccess access, TemplatePlayback templatePlayback) {
    return templatePlaybackManager.readPreviewNexusLog(access, templatePlayback);
  }

  @Override
  public Collection<Template> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    try (var selectTemplate = sqlStoreProvider.getDSL().select(TEMPLATE.fields())) {
      if (Objects.nonNull(parentIds) && !parentIds.isEmpty()) {
        if (access.isTopLevel())
          return modelsFrom(Template.class, selectTemplate
            .from(TEMPLATE)
            .where(TEMPLATE.ACCOUNT_ID.in(parentIds))
            .and(TEMPLATE.IS_DELETED.eq(false))
            .fetch());
        else
          return modelsFrom(Template.class, selectTemplate
            .from(TEMPLATE)
            .where(TEMPLATE.ACCOUNT_ID.in(parentIds))
            .and(TEMPLATE.IS_DELETED.eq(false))
            .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
            .fetch());
      } else {
        if (access.isTopLevel())
          return modelsFrom(Template.class, selectTemplate
            .from(TEMPLATE)
            .where(TEMPLATE.IS_DELETED.eq(false))
            .fetch());
        else
          return modelsFrom(Template.class, selectTemplate
            .from(TEMPLATE)
            .where(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
            .and(TEMPLATE.IS_DELETED.eq(false))
            .fetch());
      }
    } catch (Exception e) {
      throw new ManagerException(e);
    }
  }

  @Override
  public Template update(HubAccess access, UUID id, Template rawTemplate) throws ManagerException, JsonapiException, ValueException {
    Template record = validate(access, rawTemplate);
    try {
      Entities.setId(record, id); //prevent changing id
    } catch (EntityException e) {
      throw new ManagerException(e);
    }

    var db = sqlStoreProvider.getDSL();

    try (
      var selectTemplateCount = db.selectCount();
      var selectAccountCount = db.selectCount()
    ) {
      if (!access.isTopLevel()) {
        requireExists("Template",
          selectTemplateCount.from(TEMPLATE)
            .where(TEMPLATE.ID.eq(id))
            .and(TEMPLATE.IS_DELETED.eq(false))
            .fetchOne(0, int.class));
        requireExists("Account",
          selectAccountCount.from(ACCOUNT)
            .where(ACCOUNT.ID.in(access.getAccountIds()))
            .fetchOne(0, int.class));
      } else {
        requireExists("Account",
          selectAccountCount.from(ACCOUNT)
            .where(ACCOUNT.ID.eq(record.getAccountId()))
            .fetchOne(0, int.class));
      }
    } catch (Exception e) {
      throw new ManagerException(e);
    }

    try (
      var selectTemplateCount = db.selectCount()
    ) {
      requireNotExists("Template with same Ship key",
        selectTemplateCount.from(TEMPLATE)
          .where(TEMPLATE.SHIP_KEY.eq(record.getShipKey()))
          .and(TEMPLATE.ID.ne(record.getId()))
          .and(TEMPLATE.IS_DELETED.eq(false))
          .fetchOne(0, int.class));
    } catch (Exception e) {
      throw new ManagerException(e);
    }

    if (Objects.equals(TemplateType.Production, record.getType())) {
      try (var deleteTemplatePlayback = db.deleteFrom(TEMPLATE_PLAYBACK)) {
        deleteTemplatePlayback
          .where(TEMPLATE_PLAYBACK.TEMPLATE_ID.eq(id))
          .execute();
      } catch (Exception e) {
        throw new ManagerException(e);
      }
    }

    executeUpdate(sqlStoreProvider.getDSL(), TEMPLATE, id, record);
    return record;
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    DSLContext db = sqlStoreProvider.getDSL();

    Template exists = readOne(db, access, id);
    if (!TemplateType.Preview.equals(exists.getType()))
      requireTopLevel(access);

    try (var updateTemplate = db.update(TEMPLATE)
      .set(TEMPLATE.IS_DELETED, true)) {
      updateTemplate.where(TEMPLATE.ID.eq(id))
        .execute();
    } catch (Exception e) {
      throw new ManagerException(e);
    }
  }

  @Override
  public Template newInstance() {
    return new Template();
  }

  /**
   * Validate a template record
   *
   * @param access control
   * @param record to validate
   * @throws ManagerException if invalid
   */
  Template validate(HubAccess access, Template record) throws ManagerException {
    try {
      ValueUtils.require(record.getAccountId(), "Account ID");
      ValueUtils.require(record.getName(), "Name");

      // Generate a ship key if none is set
      if (StringUtils.isNullOrEmpty(record.getShipKey()))
        record.setShipKey(StringUtils.toShipKey(TremendouslyRandom.generateShipKey(GENERATED_SHIP_KEY_LENGTH)));
      else
        record.setShipKey(StringUtils.toShipKey(record.getShipKey()));

      // Default to preview chain if no type specified
      if (Objects.isNull(record.getType()))
        record.setType(TemplateType.Preview);

      // validate TypeSafe chain config https://www.pivotaltracker.com/story/show/175347578
      // Artist saves Template, Instrument, or Template config, validate & combine with defaults. https://www.pivotaltracker.com/story/show/177129498
      if (Objects.isNull(record.getConfig()))
        record.setConfig(new TemplateConfig().toString());
      else
        record.setConfig(new TemplateConfig(record).toString());

      // Only Engineer can set template to Production type, or modify a Production-type template https://www.pivotaltracker.com/story/show/178457569
      if (TemplateType.Production.equals(record.getType()))
        requireEngineer(access);

      return record;

    } catch (ValueException e) {
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
   * @throws ManagerException on failure
   */
  Template readOne(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    if (access.isTopLevel())
      try (var selectTemplate = db.selectFrom(TEMPLATE)) {
        return modelFrom(Template.class, selectTemplate
          .where(TEMPLATE.ID.eq(id))
          .and(TEMPLATE.IS_DELETED.eq(false))
          .fetchOne());
      } catch (Exception e) {
        throw new ManagerException(e);
      }
    else
      try (var selectTemplate = db.select(TEMPLATE.fields())) {
        return modelFrom(Template.class, selectTemplate
          .from(TEMPLATE)
          .where(TEMPLATE.ID.eq(id))
          .and(TEMPLATE.IS_DELETED.eq(false))
          .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne());
      } catch (Exception e) {
        throw new ManagerException(e);
      }
  }

  /**
   * Require read access
   *
   * @param db          database context
   * @param access      control
   * @param templateIds to require access to
   */
  void requireRead(DSLContext db, HubAccess access, Collection<UUID> templateIds) throws ManagerException {
    if (!access.isTopLevel())
      for (UUID templateId : templateIds)
        try (var selectCount = db.selectCount()) {
          requireExists("Template", selectCount.from(TEMPLATE)
            .where(TEMPLATE.ID.eq(templateId))
            .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
            .fetchOne(0, int.class));
        } catch (Exception e) {
          throw new ManagerException(e);
        }
  }

  /**
   * Require parent template exists of a given possible entity in a DSL context
   *
   * @param db     DSL context
   * @param access control
   * @param entity to validate
   * @throws ManagerException if parent does not exist
   */
  void requireParentExists(DSLContext db, HubAccess access, Template entity) throws ManagerException {
    try (var selectCount = db.selectCount()) {
      if (access.isTopLevel())
        requireExists("Account", selectCount.from(ACCOUNT)
          .where(ACCOUNT.ID.eq(entity.getAccountId()))
          .fetchOne(0, int.class));
      else
        requireExists("Account", selectCount.from(ACCOUNT)
          .where(ACCOUNT.ID.in(access.getAccountIds()))
          .and(ACCOUNT.ID.eq(entity.getAccountId()))
          .fetchOne(0, int.class));
    } catch (Exception e) {
      throw new ManagerException(e);
    }
  }
}
