// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.ValueUtils;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.Tables.LIBRARY;
import static io.xj.hub.Tables.PROGRAM;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_BINDING;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_BINDING_MEME;

@Service
public class ProgramSequenceBindingManagerImpl extends HubPersistenceServiceImpl implements ProgramSequenceBindingManager {

  public ProgramSequenceBindingManagerImpl(
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider
  ) {
    super(entityFactory, sqlStoreProvider);
  }

  @Override
  public ProgramSequenceBinding create(HubAccess access, ProgramSequenceBinding entity) throws ManagerException, JsonapiException, ValueException {
    validate(entity);
    requireArtist(access);
    DSLContext db = sqlStoreProvider.getDSL();
    requireProgramModification(db, access, entity.getProgramId());
    return modelFrom(ProgramSequenceBinding.class,
      executeCreate(db, PROGRAM_SEQUENCE_BINDING, entity));

  }

  @Override
  @Nullable
  public ProgramSequenceBinding readOne(HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);
    if (access.isTopLevel())
      return modelFrom(ProgramSequenceBinding.class,
        sqlStoreProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_BINDING)
          .where(PROGRAM_SEQUENCE_BINDING.ID.eq(id))
          .fetchOne());
    else
      return modelFrom(ProgramSequenceBinding.class,
        sqlStoreProvider.getDSL().select(PROGRAM_SEQUENCE_BINDING.fields()).from(PROGRAM_SEQUENCE_BINDING)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_BINDING.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramSequenceBinding> readMany(HubAccess access, Collection<UUID> programIds) throws ManagerException {
    requireArtist(access);
    if (access.isTopLevel())
      return modelsFrom(ProgramSequenceBinding.class,
        sqlStoreProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_BINDING)
          .where(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID.in(programIds))
          .fetch());
    else
      return modelsFrom(ProgramSequenceBinding.class,
        sqlStoreProvider.getDSL().select(PROGRAM_SEQUENCE_BINDING.fields()).from(PROGRAM_SEQUENCE_BINDING)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID.in(programIds))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetch());
  }

  @Override
  public ProgramSequenceBinding update(HubAccess access, UUID id, ProgramSequenceBinding entity) throws ManagerException, JsonapiException, ValueException {
    validate(entity);
    DSLContext db = sqlStoreProvider.getDSL();
    requireModification(db, access, id);
    executeUpdate(db, PROGRAM_SEQUENCE_BINDING, id, entity);
    return entity;
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);
    DSLContext db = sqlStoreProvider.getDSL();
    requireModification(db, access, id);

    requireNotExists("Meme on Sequence Binding", db.selectCount().from(PROGRAM_SEQUENCE_BINDING_MEME)
      .where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_SEQUENCE_BINDING_ID.eq(id))
      .fetchOne(0, int.class));

    db.deleteFrom(PROGRAM_SEQUENCE_BINDING)
      .where(PROGRAM_SEQUENCE_BINDING.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramSequenceBinding newInstance() {
    return new ProgramSequenceBinding();
  }

  /**
   * Require access to modification of a Program Sequence Binding
   *
   * @param db     context
   * @param access control
   * @param id     to validate access to
   * @throws ManagerException if no access
   */
  void requireModification(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);
    if (access.isTopLevel())
      requireExists("Program Sequence Binding", db.selectCount().from(PROGRAM_SEQUENCE_BINDING)
        .where(PROGRAM_SEQUENCE_BINDING.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Sequence Binding in Program in Account you have access to", db.selectCount().from(PROGRAM_SEQUENCE_BINDING)
        .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(PROGRAM_SEQUENCE_BINDING.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));
  }

  /**
   * Validate data
   *
   * @param record to validate
   * @throws ManagerException if invalid
   */
  public void validate(ProgramSequenceBinding record) throws ManagerException {
    try {
      ValueUtils.require(record.getProgramId(), "Program ID");
      ValueUtils.require(record.getProgramSequenceId(), "Sequence ID");
      ValueUtils.require(record.getOffset(), "Offset");

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

}
