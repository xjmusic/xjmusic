// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.inject.Inject;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.PlatformMessage;
import io.xj.service.hub.model.UserRoleType;
import io.xj.service.hub.persistence.SQLDatabaseProvider;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

import static io.xj.service.hub.Tables.PLATFORM_MESSAGE;

public class PlatformMessageDAOImpl extends DAOImpl<PlatformMessage> implements PlatformMessageDAO {
  private static final Long secondsPerDay = 86400L;

  @Inject
  public PlatformMessageDAOImpl(
    PayloadFactory payloadFactory,
    SQLDatabaseProvider dbProvider
  ) {
    super(payloadFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public PlatformMessage create(Access access, PlatformMessage entity) throws HubException, RestApiException, ValueException {
    requireRole("platform access", access, UserRoleType.Admin, UserRoleType.Engineer);

    entity.validate();
    return modelFrom(PlatformMessage.class, executeCreate(dbProvider.getDSL(), PLATFORM_MESSAGE, entity));
  }

  @Override
  public PlatformMessage readOne(Access access, UUID id) throws HubException {
    requireRole("platform access", access, UserRoleType.Admin, UserRoleType.Engineer);

    return modelFrom(PlatformMessage.class, dbProvider.getDSL().selectFrom(PLATFORM_MESSAGE)
      .where(PLATFORM_MESSAGE.ID.eq(id))
      .fetchOne());
  }

  @Override
  public Collection<PlatformMessage> readMany(Access access, Collection<UUID> parentIds) throws HubException {
    throw new HubException("Not allowed to read all Platform Messages (must specify # previous days).");

  }

  @Override
  public void update(Access access, UUID id, PlatformMessage entity) throws HubException {
    throw new HubException("Not allowed to update PlatformMessage record.");
  }

  @Override
  @Nullable
  public Collection<PlatformMessage> readAllPreviousDays(Access access, Integer previousDays) throws HubException {
    requireRole("platform access", access, UserRoleType.Admin, UserRoleType.Engineer);

    return modelsFrom(PlatformMessage.class, dbProvider.getDSL().select(PLATFORM_MESSAGE.fields())
      .from(PLATFORM_MESSAGE)
      .where(PLATFORM_MESSAGE.CREATED_AT.ge(Timestamp.from(Instant.now().minusSeconds(previousDays * secondsPerDay))))
      .orderBy(PLATFORM_MESSAGE.CREATED_AT.desc())
      .fetch());
  }

  @Override
  public void destroy(Access access, UUID id) throws HubException {
    DSLContext db = dbProvider.getDSL();
    requireTopLevel(access);

    requireExists("PlatformMessage", db.selectCount().from(PLATFORM_MESSAGE)
      .where(PLATFORM_MESSAGE.ID.eq(id))
      .fetchOne(0, int.class));

    db.deleteFrom(PLATFORM_MESSAGE)
      .where(PLATFORM_MESSAGE.ID.eq(id))
      .execute();
  }

  @Override
  public PlatformMessage newInstance() {
    return new PlatformMessage();
  }


}
