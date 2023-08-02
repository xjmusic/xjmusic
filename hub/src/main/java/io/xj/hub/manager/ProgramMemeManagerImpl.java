// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.ProgramMeme;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.StringUtils;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.ValueUtils;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.Tables.LIBRARY;
import static io.xj.hub.Tables.PROGRAM;
import static io.xj.hub.Tables.PROGRAM_MEME;

@Service
public class ProgramMemeManagerImpl extends HubPersistenceServiceImpl implements ProgramMemeManager {

  public ProgramMemeManagerImpl(
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider
  ) {
    super(entityFactory, sqlStoreProvider);
  }

  @Override
  public ProgramMeme create(HubAccess access, ProgramMeme rawMeme) throws ManagerException, JsonapiException, ValueException {
    var meme = validate(rawMeme);
    requireArtist(access);
    DSLContext db = sqlStoreProvider.getDSL();
    requireProgramModification(db, access, meme.getProgramId());
    return modelFrom(ProgramMeme.class,
      executeCreate(db, PROGRAM_MEME, meme));

  }

  @Override
  @Nullable
  public ProgramMeme readOne(HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);
    DSLContext db = sqlStoreProvider.getDSL();
    return readOne(db, access, id);
  }

  @Override
  @Nullable
  public Collection<ProgramMeme> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    requireArtist(access);
    if (access.isTopLevel())
      return modelsFrom(ProgramMeme.class,
        sqlStoreProvider.getDSL().selectFrom(PROGRAM_MEME)
          .where(PROGRAM_MEME.PROGRAM_ID.in(parentIds))
          .fetch());
    else
      return modelsFrom(ProgramMeme.class,
        sqlStoreProvider.getDSL().select(PROGRAM_MEME.fields()).from(PROGRAM_MEME)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_MEME.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_MEME.PROGRAM_ID.in(parentIds))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetch());
  }

  @Override
  public ProgramMeme update(HubAccess access, UUID id, ProgramMeme rawMeme) throws ManagerException, JsonapiException, ValueException {
    var meme = validate(rawMeme);
    requireArtist(access);
    DSLContext db = sqlStoreProvider.getDSL();
    var original = readOne(db, access, id);
    meme.setProgramId(original.getProgramId());
    executeUpdate(db, PROGRAM_MEME, id, meme);
    return meme;
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);
    DSLContext db = sqlStoreProvider.getDSL();
    requireModification(db, access, id);
    db.deleteFrom(PROGRAM_MEME)
      .where(PROGRAM_MEME.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramMeme newInstance() {
    return new ProgramMeme();
  }

  /**
   * Read one Program Meme that have permissions to
   *
   * @param db     context
   * @param access control
   * @param id     of entity to read
   * @return Program Meme
   * @throws ManagerException on failure
   */
  ProgramMeme readOne(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    if (access.isTopLevel())
      return modelFrom(ProgramMeme.class,
        db.selectFrom(PROGRAM_MEME)
          .where(PROGRAM_MEME.ID.eq(id))
          .fetchOne());
    else
      return modelFrom(ProgramMeme.class,
        db.select(PROGRAM_MEME.fields()).from(PROGRAM_MEME)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_MEME.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_MEME.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne());
  }

  /**
   * Require access to modification of a Program Meme
   *
   * @param db     context
   * @param access control
   * @param id     to validate access to
   * @throws ManagerException if no access
   */
  void requireModification(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);
    if (access.isTopLevel())
      requireExists("Program Meme", db.selectCount().from(PROGRAM_MEME)
        .where(PROGRAM_MEME.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Meme in Program in Account you have access to", db.selectCount().from(PROGRAM_MEME)
        .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_MEME.PROGRAM_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(PROGRAM_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));
  }

  /**
   * Validate data
   *
   * @param record to validate
   * @throws ManagerException if invalid
   */
  public ProgramMeme validate(ProgramMeme record) throws ManagerException {
    try {
      ValueUtils.require(record.getProgramId(), "Program ID");
      ValueUtils.require(record.getName(), "Name");
      record.setName(StringUtils.toMeme(record.getName()));
      return record;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

}
