// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.inject.Inject;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.Tables.TEMPLATE;
import static io.xj.hub.Tables.TEMPLATE_BINDING;

public class TemplateBindingManagerImpl extends HubPersistenceServiceImpl<TemplateBinding> implements TemplateBindingManager {

  @Inject
  public TemplateBindingManagerImpl(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(entityFactory, dbProvider);
  }

  @Override
  public TemplateBinding create(HubAccess access, TemplateBinding rawTemplateBinding) throws ManagerException, JsonapiException, ValueException {
    DSLContext db = dbProvider.getDSL();
    TemplateBinding record = validate(rawTemplateBinding);
    requireArtist(access);
    requireParentExists(db, access, record); // This entity's parent is a Template
    requireNotExists("same content already bound to template",
      db.selectCount().from(TEMPLATE_BINDING)
        .where(TEMPLATE_BINDING.TEMPLATE_ID.eq(record.getTemplateId()))
        .and(TEMPLATE_BINDING.TARGET_ID.eq(record.getTargetId()))
        .fetchOne(0, int.class));
    return modelFrom(TemplateBinding.class, executeCreate(db, TEMPLATE_BINDING, record));
  }

  @Override
  @Nullable
  public TemplateBinding readOne(HubAccess access, UUID id) throws ManagerException {
    return readOne(dbProvider.getDSL(), access, id);
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    DSLContext db = dbProvider.getDSL();

    if (!access.isTopLevel())
      requireExists("TemplateBinding belonging to you", db.selectCount().from(TEMPLATE_BINDING)
        .join(TEMPLATE).on(TEMPLATE_BINDING.TEMPLATE_ID.eq(TEMPLATE.ID))
        .where(TEMPLATE_BINDING.ID.eq(id))
        .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
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
  public Collection<TemplateBinding> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    if (access.isTopLevel())
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
        .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(TEMPLATE_BINDING.TYPE)
        .fetch());
  }

  @Override
  public TemplateBinding update(HubAccess access, UUID id, TemplateBinding rawTemplateBinding) throws ManagerException, JsonapiException, ValueException {
    throw new ManagerException("Can't update a Template Playback");
  }

  /**
   Read one record

   @param db        DSL context
   @param access control
   @param id        to read
   @return record
   @throws ManagerException on failure
   */
  private TemplateBinding readOne(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    if (access.isTopLevel())
      return modelFrom(TemplateBinding.class, db.selectFrom(TEMPLATE_BINDING)
        .where(TEMPLATE_BINDING.ID.eq(id))
        .fetchOne());
    else
      return modelFrom(TemplateBinding.class, db.select(TEMPLATE_BINDING.fields())
        .from(TEMPLATE_BINDING)
        .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_BINDING.TEMPLATE_ID))
        .where(TEMPLATE_BINDING.ID.eq(id))
        .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne());
  }

  /**
   Require parent templateBinding exists of a given possible entity in a DSL context

   @param db        DSL context
   @param access control
   @param entity    to validate
   @throws ManagerException if parent does not exist
   */
  private void requireParentExists(DSLContext db, HubAccess access, TemplateBinding entity) throws ManagerException {
    if (access.isTopLevel())
      requireExists("Template", db.selectCount().from(TEMPLATE)
        .where(TEMPLATE.ID.eq(entity.getTemplateId()))
        .fetchOne(0, int.class));
    else
      requireExists("Template", db.selectCount().from(TEMPLATE)
        .where(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
        .and(TEMPLATE.ID.eq(entity.getTemplateId()))
        .fetchOne(0, int.class));
  }

  /**
   Validate data

   @param builder to validate
   @throws ManagerException if invalid
   */
  public TemplateBinding validate(TemplateBinding builder) throws ManagerException {
    try {
      Values.require(builder.getTemplateId(), "Template ID");
      Values.require(builder.getType(), "Type");
      Values.require(builder.getTargetId(), "Target ID");

      return builder;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

}
