// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.api.client.util.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.xj.hub.TemplateConfig;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.TemplateType;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.tables.pojos.*;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.Text;
import io.xj.lib.util.TremendouslyRandom;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.hub.Tables.*;
import static io.xj.hub.tables.Account.ACCOUNT;

public class TemplateDAOImpl extends HubPersistenceServiceImpl<Template> implements TemplateDAO {
  private static final int GENERATED_SHIP_KEY_LENGTH = 9;
  private final long playbackExpireSeconds;
  private final TemplateBindingDAO templateBindingDAO;
  private final TemplatePlaybackDAO templatePlaybackDAO;
  private final TemplatePublicationDAO templatePublicationDAO;

  @Inject
  public TemplateDAOImpl(
    EntityFactory entityFactory,
    Environment env,
    HubDatabaseProvider dbProvider,
    TemplateBindingDAO templateBindingDAO,
    TemplatePlaybackDAO templatePlaybackDAO,
    TemplatePublicationDAO templatePublicationDAO
  ) {
    super(entityFactory, dbProvider);
    playbackExpireSeconds = env.getPlaybackExpireSeconds();
    this.templateBindingDAO = templateBindingDAO;
    this.templatePlaybackDAO = templatePlaybackDAO;
    this.templatePublicationDAO = templatePublicationDAO;
  }

  @Override
  public Template create(HubAccess access, Template entity) throws DAOException, JsonapiException, ValueException {
    Template record = validate(access, entity);

    var db = dbProvider.getDSL();

    if (!access.isTopLevel())
      requireExists("Account",
        db.selectCount().from(ACCOUNT)
          .where(ACCOUNT.ID.in(access.getAccountIds()))
          .fetchOne(0, int.class));

    requireNotExists("Template with same Ship key",
      db.selectCount().from(TEMPLATE)
        .where(TEMPLATE.SHIP_KEY.eq(entity.getShipKey()))
        .and(TEMPLATE.IS_DELETED.eq(false))
        .fetchOne(0, int.class));

    return modelFrom(Template.class, executeCreate(dbProvider.getDSL(), TEMPLATE, record));
  }

  @Override
  public Template readOne(HubAccess hubAccess, UUID id) throws DAOException {
    return readOne(dbProvider.getDSL(), hubAccess, id);
  }

  @Override
  public DAOCloner<Template> clone(HubAccess hubAccess, UUID rawCloneId, Template to) throws DAOException {
    requireArtist(hubAccess);
    AtomicReference<Template> result = new AtomicReference<>();
    AtomicReference<DAOCloner<Template>> cloner = new AtomicReference<>();
    dbProvider.getDSL().transaction(ctx -> {
      DSLContext db = DSL.using(ctx);

      Template from = readOne(db, hubAccess, rawCloneId);
      if (Objects.isNull(from))
        throw new DAOException("Can't clone nonexistent Template");

      // Inherits state, type if none specified
      // When not set, clone inherits attribute values from original record
      entityFactory.setAllEmptyAttributes(from, to);
      Template template = validate(hubAccess, to);
      requireParentExists(db, hubAccess, template);

      // Create main entity
      result.set(modelFrom(Template.class, executeCreate(db, TEMPLATE, template)));
      UUID originalId = result.get().getId();

      // Prepare to clone sub-entities
      cloner.set(new DAOCloner<>(result.get(), this));

      // DON'T clone TemplatePlayback- they are ephemeral, an indicator of playback state, non-existent initially

      // Clone TemplateBinding
      cloner.get().clone(db, TEMPLATE_BINDING, TEMPLATE_BINDING.ID, ImmutableSet.of(), TEMPLATE_BINDING.TEMPLATE_ID, rawCloneId, originalId);
    });
    return cloner.get();
  }

  @Override
  public Optional<Template> readOneByShipKey(HubAccess hubAccess, String rawShipKey) throws DAOException {
    String key = Text.toShipKey(rawShipKey);
    if (hubAccess.isTopLevel())
      return Optional.ofNullable(modelFrom(Template.class, dbProvider.getDSL().selectFrom(TEMPLATE)
        .where(TEMPLATE.SHIP_KEY.eq(key))
        .and(TEMPLATE.IS_DELETED.eq(false))
        .fetchOne()));
    else
      return Optional.ofNullable(modelFrom(Template.class, dbProvider.getDSL().select(TEMPLATE.fields())
        .from(TEMPLATE)
        .where(TEMPLATE.SHIP_KEY.eq(key))
        .and(TEMPLATE.IS_DELETED.eq(false))
        .and(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne()));
  }

  @Override
  public Collection<Template> readAllPlaying(HubAccess hubAccess) throws DAOException {
    requireTopLevel(hubAccess);
    DSLContext db = dbProvider.getDSL();

    return modelsFrom(Template.class, db.select(TEMPLATE.fields())
      .from(TEMPLATE)
      .join(TEMPLATE_PLAYBACK).on(TEMPLATE.ID.eq(TEMPLATE_PLAYBACK.TEMPLATE_ID))
      .where(TEMPLATE_PLAYBACK.CREATED_AT.greaterThan(Timestamp.from(Instant.now().minusSeconds(playbackExpireSeconds)).toLocalDateTime()))
      .and(TEMPLATE.IS_DELETED.eq(false))
      .fetch());
  }

  @Override
  public Collection<Object> readChildEntities(HubAccess hubAccess, Collection<UUID> templateIds, Collection<String> includeTypes) throws DAOException {
    DSLContext db = dbProvider.getDSL();

    requireRead(db, hubAccess, templateIds);

    Collection<Object> entities = Lists.newArrayList();

    // TemplateBinding
    if (includeTypes.contains(Entities.toResourceType(TemplateBinding.class)))
      entities.addAll(templateBindingDAO.readMany(hubAccess, templateIds));

    // TemplatePlayback
    if (includeTypes.contains(Entities.toResourceType(TemplatePlayback.class)))
      entities.addAll(templatePlaybackDAO.readMany(hubAccess, templateIds));

    // TemplatePublication
    if (includeTypes.contains(Entities.toResourceType(TemplatePublication.class)))
      entities.addAll(templatePublicationDAO.readMany(hubAccess, templateIds));

    // FeedbackTemplate
    if (includeTypes.contains(Entities.toResourceType(FeedbackTemplate.class)))
      entities.addAll(modelsFrom(FeedbackTemplate.class,
        db.selectFrom(FEEDBACK_TEMPLATE).where(FEEDBACK_TEMPLATE.TEMPLATE_ID.in(templateIds))));

    return entities;
  }

  @Override
  public Collection<Template> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
    if (Objects.nonNull(parentIds) && !parentIds.isEmpty()) {
      if (hubAccess.isTopLevel())
        return modelsFrom(Template.class, dbProvider.getDSL().select(TEMPLATE.fields())
          .from(TEMPLATE)
          .where(TEMPLATE.ACCOUNT_ID.in(parentIds))
          .and(TEMPLATE.IS_DELETED.eq(false))
          .fetch());
      else
        return modelsFrom(Template.class, dbProvider.getDSL().select(TEMPLATE.fields())
          .from(TEMPLATE)
          .where(TEMPLATE.ACCOUNT_ID.in(parentIds))
          .and(TEMPLATE.IS_DELETED.eq(false))
          .and(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetch());
    } else {
      if (hubAccess.isTopLevel())
        return modelsFrom(Template.class, dbProvider.getDSL().select(TEMPLATE.fields())
          .from(TEMPLATE)
          .where(TEMPLATE.IS_DELETED.eq(false))
          .fetch());
      else
        return modelsFrom(Template.class, dbProvider.getDSL().select(TEMPLATE.fields())
          .from(TEMPLATE)
          .where(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .and(TEMPLATE.IS_DELETED.eq(false))
          .fetch());
    }

  }

  @Override
  public Template update(HubAccess access, UUID id, Template rawTemplate) throws DAOException, JsonapiException, ValueException {
    Template record = validate(access, rawTemplate);
    try {
      Entities.setId(record, id); //prevent changing id
    } catch (EntityException e) {
      throw new DAOException(e);
    }

    var db = dbProvider.getDSL();

    if (!access.isTopLevel()) {
      requireExists("Template",
        db.selectCount().from(TEMPLATE)
          .where(TEMPLATE.ID.eq(id))
          .and(TEMPLATE.IS_DELETED.eq(false))
          .fetchOne(0, int.class));
      requireExists("Account",
        db.selectCount().from(ACCOUNT)
          .where(ACCOUNT.ID.in(access.getAccountIds()))
          .fetchOne(0, int.class));
    }

    requireNotExists("Template with same Ship key",
      db.selectCount().from(TEMPLATE)
        .where(TEMPLATE.SHIP_KEY.eq(record.getShipKey()))
        .and(TEMPLATE.ID.ne(record.getId()))
        .and(TEMPLATE.IS_DELETED.eq(false))
        .fetchOne(0, int.class));

    executeUpdate(dbProvider.getDSL(), TEMPLATE, id, record);
    return record;
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
    DSLContext db = dbProvider.getDSL();
    requireTopLevel(hubAccess);

    db.update(TEMPLATE)
      .set(TEMPLATE.IS_DELETED, true)
      .where(TEMPLATE.ID.eq(id))
      .execute();
  }

  @Override
  public Template newInstance() {
    return new Template();
  }

  /**
   Validate a template record

   @param access control
   @param record to validate
   @throws DAOException if invalid
   */
  private Template validate(HubAccess access, Template record) throws DAOException {
    try {
      Values.require(record.getAccountId(), "Account ID");
      Values.require(record.getName(), "Name");

      // Generate a ship key if none is set
      if (Strings.isNullOrEmpty(record.getShipKey()))
        record.setShipKey(Text.toShipKey(TremendouslyRandom.generateShipKey(GENERATED_SHIP_KEY_LENGTH)));
      else
        record.setShipKey(Text.toShipKey(record.getShipKey()));

      // Default to preview chain if no type specified
      if (Objects.isNull(record.getType()))
        record.setType(TemplateType.Preview);

      // [#175347578] validate TypeSafe chain config
      // [#177129498] Artist saves Template, Instrument, or Template config, validate & combine with defaults.
      if (Objects.isNull(record.getConfig()))
        record.setConfig(new TemplateConfig().toString());
      else
        record.setConfig(new TemplateConfig(record).toString());

      // [#178457569] Only Engineer can set template to Production type, or modify a Production-type template
      if (TemplateType.Production.equals(record.getType()))
        requireEngineer(access);

      return record;

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

  /**
   Read one record

   @param db        DSL context
   @param hubAccess control
   @param id        to read
   @return record
   @throws DAOException on failure
   */
  private Template readOne(DSLContext db, HubAccess hubAccess, UUID id) throws DAOException {
    if (hubAccess.isTopLevel())
      return modelFrom(Template.class, db.selectFrom(TEMPLATE)
        .where(TEMPLATE.ID.eq(id))
        .and(TEMPLATE.IS_DELETED.eq(false))
        .fetchOne());
    else
      return modelFrom(Template.class, db.select(TEMPLATE.fields())
        .from(TEMPLATE)
        .where(TEMPLATE.ID.eq(id))
        .and(TEMPLATE.IS_DELETED.eq(false))
        .and(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne());
  }

  /**
   Require read hubAccess

   @param db          database context
   @param hubAccess   control
   @param templateIds to require hubAccess to
   */
  private void requireRead(DSLContext db, HubAccess hubAccess, Collection<UUID> templateIds) throws DAOException {
    if (!hubAccess.isTopLevel())
      for (UUID templateId : templateIds)
        requireExists("Template", db.selectCount().from(TEMPLATE)
          .where(TEMPLATE.ID.eq(templateId))
          .and(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetchOne(0, int.class));
  }

  /**
   Require parent template exists of a given possible entity in a DSL context

   @param db        DSL context
   @param hubAccess control
   @param entity    to validate
   @throws DAOException if parent does not exist
   */
  private void requireParentExists(DSLContext db, HubAccess hubAccess, Template entity) throws DAOException {
    if (hubAccess.isTopLevel())
      requireExists("Account", db.selectCount().from(ACCOUNT)
        .where(ACCOUNT.ID.eq(entity.getAccountId()))
        .fetchOne(0, int.class));
    else
      requireExists("Account", db.selectCount().from(ACCOUNT)
        .where(ACCOUNT.ID.in(hubAccess.getAccountIds()))
        .and(ACCOUNT.ID.eq(entity.getAccountId()))
        .fetchOne(0, int.class));
  }

}
