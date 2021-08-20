// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.inject.Inject;
import io.xj.api.TemplateBinding;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.Tables.TEMPLATE;
import static io.xj.hub.Tables.TEMPLATE_BINDING;

public class TemplateBindingDAOImpl extends DAOImpl<TemplateBinding> implements TemplateBindingDAO {

  @Inject
  public TemplateBindingDAOImpl(
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public TemplateBinding create(HubAccess hubAccess, TemplateBinding rawTemplateBinding) throws DAOException, JsonapiException, ValueException {
    DSLContext db = dbProvider.getDSL();
    TemplateBinding record = validate(rawTemplateBinding);
    requireArtist(hubAccess);
    requireParentExists(db, hubAccess, record); // This entity's parent is a Template
    requireNotExists("same content already bound to template",
      db.selectCount().from(TEMPLATE_BINDING)
        .where(TEMPLATE_BINDING.TEMPLATE_ID.eq(record.getTemplateId()))
        .and(TEMPLATE_BINDING.TARGET_ID.eq(record.getTargetId()))
        .fetchOne(0, int.class));
    return modelFrom(TemplateBinding.class, executeCreate(db, TEMPLATE_BINDING, record));
  }

  @Override
  @Nullable
  public TemplateBinding readOne(HubAccess hubAccess, UUID id) throws DAOException {
    return readOne(dbProvider.getDSL(), hubAccess, id);
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
    DSLContext db = dbProvider.getDSL();

    if (!hubAccess.isTopLevel())
      requireExists("TemplateBinding belonging to you", db.selectCount().from(TEMPLATE_BINDING)
        .join(TEMPLATE).on(TEMPLATE_BINDING.TEMPLATE_ID.eq(TEMPLATE.ID))
        .where(TEMPLATE_BINDING.ID.eq(id))
        .and(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne(0, int.class));

    //
    // [#170299297] Cannot delete TemplateBindings that have a Meme-- otherwise, destroy all inner entities
    //

    db.deleteFrom(TEMPLATE_BINDING)
      .where(TEMPLATE_BINDING.ID.eq(id))
      .execute();
  }

  @Override
  public TemplateBinding newInstance() {
    return new TemplateBinding();
  }

  @Override
  public Collection<TemplateBinding> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
    if (hubAccess.isTopLevel())
      return modelsFrom(TemplateBinding.class, dbProvider.getDSL().select(TEMPLATE_BINDING.fields())
        .from(TEMPLATE_BINDING)
        .where(TEMPLATE_BINDING.TEMPLATE_ID.in(parentIds))
        .orderBy(TEMPLATE_BINDING.TYPE)
        .fetch());
    else
      return modelsFrom(TemplateBinding.class, dbProvider.getDSL().select(TEMPLATE_BINDING.fields())
        .from(TEMPLATE_BINDING)
        .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_BINDING.TEMPLATE_ID))
        .where(TEMPLATE_BINDING.TEMPLATE_ID.in(parentIds))
        .and(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .orderBy(TEMPLATE_BINDING.TYPE)
        .fetch());
  }

  @Override
  public TemplateBinding update(HubAccess hubAccess, UUID id, TemplateBinding rawTemplateBinding) throws DAOException, JsonapiException, ValueException {
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
  private TemplateBinding readOne(DSLContext db, HubAccess hubAccess, UUID id) throws DAOException {
    if (hubAccess.isTopLevel())
      return modelFrom(TemplateBinding.class, db.selectFrom(TEMPLATE_BINDING)
        .where(TEMPLATE_BINDING.ID.eq(id))
        .fetchOne());
    else
      return modelFrom(TemplateBinding.class, db.select(TEMPLATE_BINDING.fields())
        .from(TEMPLATE_BINDING)
        .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_BINDING.TEMPLATE_ID))
        .where(TEMPLATE_BINDING.ID.eq(id))
        .and(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne());
  }

  /**
   Require parent templateBinding exists of a given possible entity in a DSL context

   @param db        DSL context
   @param hubAccess control
   @param entity    to validate
   @throws DAOException if parent does not exist
   */
  private void requireParentExists(DSLContext db, HubAccess hubAccess, TemplateBinding entity) throws DAOException {
    if (hubAccess.isTopLevel())
      requireExists("Template", db.selectCount().from(TEMPLATE)
        .where(TEMPLATE.ID.eq(entity.getTemplateId()))
        .fetchOne(0, int.class));
    else
      requireExists("Template", db.selectCount().from(TEMPLATE)
        .where(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .and(TEMPLATE.ID.eq(entity.getTemplateId()))
        .fetchOne(0, int.class));
  }

  /**
   Validate data

   @param builder to validate
   @throws DAOException if invalid
   */
  public TemplateBinding validate(TemplateBinding builder) throws DAOException {
    try {
      Value.require(builder.getTemplateId(), "Template ID");
      Value.require(builder.getType(), "Type");
      Value.require(builder.getTargetId(), "Target ID");

      return builder;

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

}
