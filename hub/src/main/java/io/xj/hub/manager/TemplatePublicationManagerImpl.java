// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.inject.Inject;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.TemplateType;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplatePublication;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static io.xj.hub.Tables.TEMPLATE;
import static io.xj.hub.Tables.TEMPLATE_PUBLICATION;

public class TemplatePublicationManagerImpl extends HubPersistenceServiceImpl<TemplatePublication> implements TemplatePublicationManager {
  @Inject
  public TemplatePublicationManagerImpl(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(entityFactory, dbProvider);
  }

  @Override
  public TemplatePublication create(HubAccess hubAccess, TemplatePublication raw) throws ManagerException, JsonapiException, ValueException {
    DSLContext db = dbProvider.getDSL();
    raw.setUserId(hubAccess.getUserId());
    TemplatePublication record = validate(raw);
    requireArtist(hubAccess);

    if (!hubAccess.isTopLevel())
      requireExists("Access to template", db.selectCount().from(TEMPLATE)
        .where(TEMPLATE.ID.eq(record.getTemplateId()))
        .and(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne(0, int.class));

    var template = modelFrom(Template.class, dbProvider.getDSL().selectFrom(TEMPLATE)
      .where(TEMPLATE.ID.eq(record.getTemplateId()))
      .fetchOne());
    requireAny("Production-type Template", TemplateType.Production.equals(template.getType()));

    for (var prior : modelsFrom(TemplatePublication.class,
      db.selectFrom(TEMPLATE_PUBLICATION)
        .where(TEMPLATE_PUBLICATION.TEMPLATE_ID.eq(record.getTemplateId()))
        .fetch()))
      db.deleteFrom(TEMPLATE_PUBLICATION)
        .where(TEMPLATE_PUBLICATION.ID.eq(prior.getId()))
        .execute();
    return modelFrom(TemplatePublication.class, executeCreate(db, TEMPLATE_PUBLICATION, record));
  }

  @Override
  @Nullable
  public TemplatePublication readOne(HubAccess hubAccess, UUID id) throws ManagerException {
    return readOne(dbProvider.getDSL(), hubAccess, id);
  }

  @Override
  public Optional<TemplatePublication> readOneForUser(HubAccess hubAccess, UUID userId) throws ManagerException {
    DSLContext db = dbProvider.getDSL();
    var publicationRecord = hubAccess.isTopLevel()
      ?
      db.selectFrom(TEMPLATE_PUBLICATION)
        .where(TEMPLATE_PUBLICATION.USER_ID.eq(userId))
        .fetchOne()
      :
      db.select(TEMPLATE_PUBLICATION.fields())
        .from(TEMPLATE_PUBLICATION)
        .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_PUBLICATION.TEMPLATE_ID))
        .where(TEMPLATE_PUBLICATION.USER_ID.eq(userId))
        .and(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne();

    return Objects.nonNull(publicationRecord) ? Optional.of(modelFrom(TemplatePublication.class, publicationRecord)) : Optional.empty();
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws ManagerException {
    throw new ManagerException("Cannot delete template publication!");
  }

  @Override
  public TemplatePublication newInstance() {
    return new TemplatePublication();
  }

  @Override
  public Collection<TemplatePublication> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws ManagerException {
    if (hubAccess.isTopLevel())
      return modelsFrom(TemplatePublication.class, dbProvider.getDSL().select(TEMPLATE_PUBLICATION.fields())
        .from(TEMPLATE_PUBLICATION)
        .where(TEMPLATE_PUBLICATION.TEMPLATE_ID.in(parentIds))
        .orderBy(TEMPLATE_PUBLICATION.USER_ID)
        .fetch());
    else
      return modelsFrom(TemplatePublication.class, dbProvider.getDSL().select(TEMPLATE_PUBLICATION.fields())
        .from(TEMPLATE_PUBLICATION)
        .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_PUBLICATION.TEMPLATE_ID))
        .where(TEMPLATE_PUBLICATION.TEMPLATE_ID.in(parentIds))
        .and(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .orderBy(TEMPLATE_PUBLICATION.USER_ID)
        .fetch());
  }

  @Override
  public TemplatePublication update(HubAccess hubAccess, UUID id, TemplatePublication rawTemplatePublication) throws ManagerException, JsonapiException, ValueException {
    throw new ManagerException("Can't update a Template Publication");
  }

  /**
   Read one record

   @param db        DSL context
   @param hubAccess control
   @param id        to read
   @return record
   @throws ManagerException on failure
   */
  private TemplatePublication readOne(DSLContext db, HubAccess hubAccess, UUID id) throws ManagerException {
    if (hubAccess.isTopLevel())
      return modelFrom(TemplatePublication.class, db.selectFrom(TEMPLATE_PUBLICATION)
        .where(TEMPLATE_PUBLICATION.ID.eq(id))
        .fetchOne());
    else
      return modelFrom(TemplatePublication.class, db.select(TEMPLATE_PUBLICATION.fields())
        .from(TEMPLATE_PUBLICATION)
        .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_PUBLICATION.TEMPLATE_ID))
        .where(TEMPLATE_PUBLICATION.ID.eq(id))
        .and(TEMPLATE.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne());
  }

  /**
   Validate data

   @param builder to validate
   @throws ManagerException if invalid
   */
  public TemplatePublication validate(TemplatePublication builder) throws ManagerException {
    try {
      Values.require(builder.getTemplateId(), "Template ID");
      Values.require(builder.getUserId(), "User ID");

      return builder;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

}
