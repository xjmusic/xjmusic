// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.api.client.util.Strings;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.api.Template;
import io.xj.api.TemplateType;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.TemplateConfig;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.Text;
import io.xj.lib.util.TremendouslyRandom;
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
import static io.xj.hub.Tables.TEMPLATE_BINDING;
import static io.xj.hub.Tables.TEMPLATE_PLAYBACK;
import static io.xj.hub.tables.Account.ACCOUNT;

public class TemplateDAOImpl extends DAOImpl<Template> implements TemplateDAO {
  private static final int GENERATED_EMBED_KEY_LENGTH = 9;
  private final Config config;
  private final long playbackExpireSeconds;

  @Inject
  public TemplateDAOImpl(
    Config config,
    EntityFactory entityFactory,
    Environment env,
    HubDatabaseProvider dbProvider,
    JsonapiPayloadFactory payloadFactory
  ) {
    super(payloadFactory, entityFactory);
    playbackExpireSeconds = env.getPlaybackExpireSeconds();
    this.config = config;
    this.dbProvider = dbProvider;
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

    requireNotExists("Template with same Embed Key",
      db.selectCount().from(TEMPLATE)
        .where(TEMPLATE.EMBED_KEY.eq(entity.getEmbedKey()))
        .fetchOne(0, int.class));

    return modelFrom(Template.class, executeCreate(dbProvider.getDSL(), TEMPLATE, record));
  }

  @Override
  @Nullable
  public Template readOne(HubAccess hubAccess, UUID id) throws DAOException {
    if (hubAccess.isTopLevel())
      return modelFrom(Template.class, dbProvider.getDSL().selectFrom(TEMPLATE)
        .where(TEMPLATE.ID.eq(id))
        .fetchOne());
    else
      return modelFrom(Template.class, dbProvider.getDSL().select(TEMPLATE.fields())
        .from(TEMPLATE)
        .where(TEMPLATE.ID.eq(id))
        .and(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne());
  }

  @Override
  public Optional<Template> readOneByEmbedKey(HubAccess hubAccess, String rawEmbedKey) throws DAOException {
    String key = Text.toEmbedKey(rawEmbedKey);
    if (hubAccess.isTopLevel())
      return Optional.ofNullable(modelFrom(Template.class, dbProvider.getDSL().selectFrom(TEMPLATE)
        .where(TEMPLATE.EMBED_KEY.eq(key))
        .fetchOne()));
    else
      return Optional.ofNullable(modelFrom(Template.class, dbProvider.getDSL().select(TEMPLATE.fields())
        .from(TEMPLATE)
        .where(TEMPLATE.EMBED_KEY.eq(key))
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
      .where(TEMPLATE_PLAYBACK.CREATED_AT.greaterThan(Timestamp.from(Instant.now().minusSeconds(playbackExpireSeconds))))
      .fetch());
  }

  @Override
  public Collection<Template> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
    if (Objects.nonNull(parentIds) && !parentIds.isEmpty()) {
      if (hubAccess.isTopLevel())
        return modelsFrom(Template.class, dbProvider.getDSL().select(TEMPLATE.fields())
          .from(TEMPLATE)
          .where(TEMPLATE.ACCOUNT_ID.in(parentIds))
          .fetch());
      else
        return modelsFrom(Template.class, dbProvider.getDSL().select(TEMPLATE.fields())
          .from(TEMPLATE)
          .where(TEMPLATE.ACCOUNT_ID.in(parentIds))
          .and(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetch());
    } else {
      if (hubAccess.isTopLevel())
        return modelsFrom(Template.class, dbProvider.getDSL().select(TEMPLATE.fields())
          .from(TEMPLATE)
          .fetch());
      else
        return modelsFrom(Template.class, dbProvider.getDSL().select(TEMPLATE.fields())
          .from(TEMPLATE)
          .where(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
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
          .fetchOne(0, int.class));
      requireExists("Account",
        db.selectCount().from(ACCOUNT)
          .where(ACCOUNT.ID.in(access.getAccountIds()))
          .fetchOne(0, int.class));
    }

    requireNotExists("Template with same Embed Key",
      db.selectCount().from(TEMPLATE)
        .where(TEMPLATE.EMBED_KEY.eq(record.getEmbedKey()))
        .and(TEMPLATE.ID.ne(record.getId()))
        .fetchOne(0, int.class));

    executeUpdate(dbProvider.getDSL(), TEMPLATE, id, record);
    return record;
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
    DSLContext db = dbProvider.getDSL();
    requireTopLevel(hubAccess);

    db.deleteFrom(TEMPLATE_BINDING)
      .where(TEMPLATE_BINDING.TEMPLATE_ID.eq(id))
      .execute();

    db.deleteFrom(TEMPLATE_PLAYBACK)
      .where(TEMPLATE_PLAYBACK.TEMPLATE_ID.eq(id))
      .execute();

    db.deleteFrom(TEMPLATE)
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
  public Template validate(HubAccess access, Template record) throws DAOException {
    try {
      Value.require(record.getAccountId(), "Account ID");
      Value.require(record.getName(), "Name");

      // Generate an embed key if none is set
      if (Strings.isNullOrEmpty(record.getEmbedKey()))
        record.setEmbedKey(Text.toEmbedKey(TremendouslyRandom.generateEmbedKey(GENERATED_EMBED_KEY_LENGTH)));
      else
        record.setEmbedKey(Text.toEmbedKey(record.getEmbedKey()));

      // Default to preview chain if no type specified
      if (Objects.isNull(record.getType()))
        record.setType(TemplateType.PREVIEW);

      // [#175347578] validate TypeSafe chain config
      // [#177129498] Artist saves Template, Instrument, or Template config, validate & combine with defaults.
      if (Objects.isNull(record.getConfig()))
        record.setConfig(new TemplateConfig(config).toString());
      else
        record.setConfig(new TemplateConfig(record, config).toString());

      // [#178457569] Only Engineer can set template to Production type, or modify a Production-type template
      if (TemplateType.PRODUCTION.equals(record.getType()))
        requireEngineer(access);

      return record;

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

}
