// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.TemplateType;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplatePublication;
import io.xj.hub.tables.records.TemplatePublicationRecord;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.ValueUtils;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.stereotype.Service;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static io.xj.hub.Tables.TEMPLATE;
import static io.xj.hub.Tables.TEMPLATE_PUBLICATION;

@Service
public class TemplatePublicationManagerImpl extends HubPersistenceServiceImpl implements TemplatePublicationManager {
  public TemplatePublicationManagerImpl(
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider
  ) {
    super(entityFactory, sqlStoreProvider);
  }

  @Override
  public TemplatePublication create(HubAccess access, TemplatePublication raw) throws ManagerException, JsonapiException, ValueException {
    DSLContext db = sqlStoreProvider.getDSL();
    raw.setUserId(access.getUserId());
    TemplatePublication record = validate(raw);
    requireArtist(access);

    if (!access.isTopLevel())
      try (var selectCount = db.selectCount()) {
        requireExists("Access to template", selectCount.from(TEMPLATE)
          .where(TEMPLATE.ID.eq(record.getTemplateId()))
          .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne(0, int.class));
      } catch (Exception e) {
        throw new ManagerException(e);
      }

    Template template;
    try (var selectTemplate = sqlStoreProvider.getDSL().selectFrom(TEMPLATE)) {
      template = modelFrom(Template.class,
        selectTemplate
          .where(TEMPLATE.ID.eq(record.getTemplateId()))
          .fetchOne());
      requireAny("Production-type Template", TemplateType.Production.equals(template.getType()));
    } catch (Exception e) {
      throw new ManagerException(e);
    }

    Result<TemplatePublicationRecord> toDelete;
    try (var selectTemplatePublication = db.selectFrom(TEMPLATE_PUBLICATION)) {
      toDelete =
        selectTemplatePublication
          .where(TEMPLATE_PUBLICATION.TEMPLATE_ID.eq(record.getTemplateId()))
          .fetch();
    } catch (Exception e) {
      throw new ManagerException(e);
    }
    for (var prior : modelsFrom(TemplatePublication.class, toDelete))
      try (var deleteTemplatePublication = db.deleteFrom(TEMPLATE_PUBLICATION)) {
        deleteTemplatePublication
          .where(TEMPLATE_PUBLICATION.ID.eq(prior.getId()))
          .execute();
      } catch (Exception e) {
        throw new ManagerException(e);
      }
    return modelFrom(TemplatePublication.class, executeCreate(db, TEMPLATE_PUBLICATION, record));
  }

  @Override
  @Nullable
  public TemplatePublication readOne(HubAccess access, UUID id) throws ManagerException {
    return readOne(sqlStoreProvider.getDSL(), access, id);
  }

  @Override
  public Optional<TemplatePublication> readOneForUser(HubAccess access, UUID userId) throws ManagerException {
    DSLContext db = sqlStoreProvider.getDSL();
    Record record;
    if (access.isTopLevel())
      try (var selectTemplatePublication = db.selectFrom(TEMPLATE_PUBLICATION)) {
        record = selectTemplatePublication
          .where(TEMPLATE_PUBLICATION.USER_ID.eq(userId))
          .fetchOne();

      } catch (Exception e) {
        throw new ManagerException(e);
      }
    else
      try (var selectTemplatePublication = db.select(TEMPLATE_PUBLICATION.fields());
           var joinTemplate =
             selectTemplatePublication
               .from(TEMPLATE_PUBLICATION)
               .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_PUBLICATION.TEMPLATE_ID))) {
        record = joinTemplate
          .where(TEMPLATE_PUBLICATION.USER_ID.eq(userId))
          .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne();

      } catch (Exception e) {
        throw new ManagerException(e);
      }

    return Objects.nonNull(record) ? Optional.of(modelFrom(TemplatePublication.class, record)) : Optional.empty();
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    throw new ManagerException("Cannot delete template publication!");
  }

  @Override
  public TemplatePublication newInstance() {
    return new TemplatePublication();
  }

  @Override
  public Collection<TemplatePublication> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    if (access.isTopLevel())
      try (var selectTemplatePublication = sqlStoreProvider.getDSL().select(TEMPLATE_PUBLICATION.fields())) {
        return modelsFrom(TemplatePublication.class,
          selectTemplatePublication
            .from(TEMPLATE_PUBLICATION)
            .where(TEMPLATE_PUBLICATION.TEMPLATE_ID.in(parentIds))
            .orderBy(TEMPLATE_PUBLICATION.USER_ID)
            .fetch());
      } catch (Exception e) {
        throw new ManagerException(e);
      }
    else
      try (var selectTemplatePublication = sqlStoreProvider.getDSL().select(TEMPLATE_PUBLICATION.fields());
           var joinTemplate =
             selectTemplatePublication
               .from(TEMPLATE_PUBLICATION)
               .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_PUBLICATION.TEMPLATE_ID))) {
        return modelsFrom(TemplatePublication.class,
          joinTemplate
            .where(TEMPLATE_PUBLICATION.TEMPLATE_ID.in(parentIds))
            .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
            .orderBy(TEMPLATE_PUBLICATION.USER_ID)
            .fetch());
      }
  }

  @Override
  public TemplatePublication update(HubAccess access, UUID id, TemplatePublication rawTemplatePublication) throws ManagerException, JsonapiException, ValueException {
    throw new ManagerException("Can't update a Template Publication");
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
  TemplatePublication readOne(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    if (access.isTopLevel())
      try (var S = db.selectFrom(TEMPLATE_PUBLICATION)) {
        return modelFrom(TemplatePublication.class, S
          .where(TEMPLATE_PUBLICATION.ID.eq(id))
          .fetchOne());
      } catch (Exception e) {
        throw new ManagerException(e);
      }
    else
      try (var selectTemplatePublication = db.select(TEMPLATE_PUBLICATION.fields());
           var joinTemplate = selectTemplatePublication
             .from(TEMPLATE_PUBLICATION)
             .join(TEMPLATE).on(TEMPLATE.ID.eq(TEMPLATE_PUBLICATION.TEMPLATE_ID))) {
        return modelFrom(TemplatePublication.class, joinTemplate
          .where(TEMPLATE_PUBLICATION.ID.eq(id))
          .and(TEMPLATE.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne());
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
  public TemplatePublication validate(TemplatePublication builder) throws ManagerException {
    try {
      ValueUtils.require(builder.getTemplateId(), "Template ID");
      ValueUtils.require(builder.getUserId(), "User ID");

      return builder;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

}
