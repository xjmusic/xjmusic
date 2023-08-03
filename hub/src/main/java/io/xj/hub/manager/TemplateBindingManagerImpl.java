// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.ValueUtils;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.Tables.TEMPLATE;
import static io.xj.hub.Tables.TEMPLATE_BINDING;

@Service
public class TemplateBindingManagerImpl extends HubPersistenceServiceImpl implements TemplateBindingManager {

  public TemplateBindingManagerImpl(
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider
  ) {
    super(entityFactory, sqlStoreProvider);
  }

  @Override
  public TemplateBinding create(HubAccess access, TemplateBinding rawTemplateBinding) throws ManagerException, JsonapiException, ValueException {
    DSLContext db = sqlStoreProvider.getDSL();
    TemplateBinding record = validate(rawTemplateBinding);
    requireArtist(access);
    requireParentExists(db, access, record); // This entity's parent is a Template
    try (var selectCount = db.selectCount()) {
      requireNotExists("same content already bound to template",
        selectCount.from(TEMPLATE_BINDING)
          .where(TEMPLATE_BINDING.TEMPLATE_ID.eq(record.getTemplateId()))
          .and(TEMPLATE_BINDING.TARGET_ID.eq(record.getTargetId()))
          .fetchOne(0, int.class));
    } catch (Exception e) {
      throw new ManagerException(e);
    }
    return modelFrom(TemplateBinding.class, executeCreate(db, TEMPLATE_BINDING, record));
  }

  @Override
  @Nullable
  public TemplateBinding readOne(HubAccess access, UUID id) throws ManagerException {
    return readOne(sqlStoreProvider.getDSL(), access, id);
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    DSLContext db = sqlStoreProvider.getDSL();

    try (var selectCount = db.selectCount();
         var joinTemplateBinding = selectCount.from(TEMPLATE_BINDING)
           .join(TEMPLATE).on(TEMPLATE_BINDING.TEMPLATE_ID.eq(TEMPLATE.ID))) {
      if (!access.isTopLevel())
        requireExists("TemplateBinding belonging to you",
          joinTemplateBinding
            .where(TEMPLATE_BINDING.ID.eq(id))
            .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
            .fetchOne(0, int.class));
    } catch (Exception e) {
      throw new ManagerException(e);
    }

    //
    // Cannot delete TemplateBindings that have a Meme-- otherwise, destroy all inner entities https://www.pivotaltracker.com/story/show/170299297
    //

    try (var deleteTemplateBinding = db.deleteFrom(TEMPLATE_BINDING)) {
      deleteTemplateBinding.where(TEMPLATE_BINDING.ID.eq(id))
        .execute();
    } catch (Exception e) {
      throw new ManagerException(e);
    }

  }

  @Override
  public TemplateBinding newInstance() {
    return new TemplateBinding();
  }

  @Override
  public Collection<TemplateBinding> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    if (access.isTopLevel())
      try (var selectTemplateBinding = sqlStoreProvider.getDSL().select(TEMPLATE_BINDING.fields())) {
        return modelsFrom(TemplateBinding.class,
          selectTemplateBinding
            .from(TEMPLATE_BINDING)
            .where(TEMPLATE_BINDING.TEMPLATE_ID.in(parentIds))
            .orderBy(TEMPLATE_BINDING.TYPE)
            .fetch());
      } catch (Exception e) {
        throw new ManagerException(e);
      }
    else
      try (
        var selectTemplateBinding = sqlStoreProvider.getDSL().select(TEMPLATE_BINDING.fields());
        var joinTemplate = selectTemplateBinding
          .from(TEMPLATE_BINDING)
          .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_BINDING.TEMPLATE_ID))
      ) {
        return modelsFrom(TemplateBinding.class,
          joinTemplate
            .where(TEMPLATE_BINDING.TEMPLATE_ID.in(parentIds))
            .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
            .orderBy(TEMPLATE_BINDING.TYPE)
            .fetch());
      } catch (Exception e) {
        throw new ManagerException(e);
      }

  }

  @Override
  public TemplateBinding update(HubAccess access, UUID id, TemplateBinding rawTemplateBinding) throws ManagerException, JsonapiException, ValueException {
    throw new ManagerException("Can't update a Template Playback");
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
  TemplateBinding readOne(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    if (access.isTopLevel())
      try (var selectTemplateBinding = db.selectFrom(TEMPLATE_BINDING)) {
        return modelFrom(TemplateBinding.class,
          selectTemplateBinding
            .where(TEMPLATE_BINDING.ID.eq(id))
            .fetchOne());
      } catch (Exception e) {
        throw new ManagerException(e);
      }

    else
      try (
        var selectTemplateBinding = db.select(TEMPLATE_BINDING.fields());
        var joinTemplate = selectTemplateBinding
          .from(TEMPLATE_BINDING)
          .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_BINDING.TEMPLATE_ID))
      ) {
        return modelFrom(TemplateBinding.class,
          joinTemplate
            .where(TEMPLATE_BINDING.ID.eq(id))
            .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
            .fetchOne());
      } catch (Exception e) {
        throw new ManagerException(e);
      }
  }

  /**
   * Require parent templateBinding exists of a given possible entity in a DSL context
   *
   * @param db     DSL context
   * @param access control
   * @param entity to validate
   * @throws ManagerException if parent does not exist
   */
  void requireParentExists(DSLContext db, HubAccess access, TemplateBinding entity) throws ManagerException {
    try (var selectCount = db.selectCount()) {
      if (access.isTopLevel())
        requireExists("Template", selectCount.from(TEMPLATE)
          .where(TEMPLATE.ID.eq(entity.getTemplateId()))
          .fetchOne(0, int.class));
      else
        requireExists("Template", selectCount.from(TEMPLATE)
          .where(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
          .and(TEMPLATE.ID.eq(entity.getTemplateId()))
          .fetchOne(0, int.class));
    } catch (Exception e) {
      throw new ManagerException(e);
    }
  }

  /**
   * Validate data
   *
   * @param builder to validate
   * @throws ManagerException if invalid
   */
  public TemplateBinding validate(TemplateBinding builder) throws ManagerException {
    try {
      ValueUtils.require(builder.getTemplateId(), "Template ID");
      ValueUtils.require(builder.getType(), "Type");
      ValueUtils.require(builder.getTargetId(), "Target ID");

      return builder;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

}
