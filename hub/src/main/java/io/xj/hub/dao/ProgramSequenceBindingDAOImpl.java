// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.inject.Inject;
import io.xj.api.ProgramSequenceBinding;
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

import static io.xj.hub.Tables.LIBRARY;
import static io.xj.hub.Tables.PROGRAM;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_BINDING;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_BINDING_MEME;

public class ProgramSequenceBindingDAOImpl extends DAOImpl<ProgramSequenceBinding> implements ProgramSequenceBindingDAO {

  @Inject
  public ProgramSequenceBindingDAOImpl(
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramSequenceBinding create(HubAccess hubAccess, ProgramSequenceBinding entity) throws DAOException, JsonapiException, ValueException {
    validate(entity);
    requireArtist(hubAccess);
    DSLContext db = dbProvider.getDSL();
    requireProgramModification(db, hubAccess, entity.getProgramId());
    return modelFrom(ProgramSequenceBinding.class,
      executeCreate(db, PROGRAM_SEQUENCE_BINDING, entity));

  }

  @Override
  @Nullable
  public ProgramSequenceBinding readOne(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    if (hubAccess.isTopLevel())
      return modelFrom(ProgramSequenceBinding.class,
        dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_BINDING)
          .where(PROGRAM_SEQUENCE_BINDING.ID.eq(id))
          .fetchOne());
    else
      return modelFrom(ProgramSequenceBinding.class,
        dbProvider.getDSL().select(PROGRAM_SEQUENCE_BINDING.fields()).from(PROGRAM_SEQUENCE_BINDING)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_BINDING.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramSequenceBinding> readMany(HubAccess hubAccess, Collection<UUID> programIds) throws DAOException {
    requireArtist(hubAccess);
    if (hubAccess.isTopLevel())
      return modelsFrom(ProgramSequenceBinding.class,
        dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_BINDING)
          .where(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID.in(programIds))
          .fetch());
    else
      return modelsFrom(ProgramSequenceBinding.class,
        dbProvider.getDSL().select(PROGRAM_SEQUENCE_BINDING.fields()).from(PROGRAM_SEQUENCE_BINDING)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID.in(programIds))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetch());
  }

  @Override
  public ProgramSequenceBinding update(HubAccess hubAccess, UUID id, ProgramSequenceBinding entity) throws DAOException, JsonapiException, ValueException {
    validate(entity);
    DSLContext db = dbProvider.getDSL();
    requireModification(db, hubAccess, id);
    executeUpdate(db, PROGRAM_SEQUENCE_BINDING, id, entity);
    return entity;
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    DSLContext db = dbProvider.getDSL();
    requireModification(db, hubAccess, id);

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
   Require hubAccess to modification of a Program Sequence Binding

   @param db        context
   @param hubAccess control
   @param id        to validate hubAccess to
   @throws DAOException if no hubAccess
   */
  private void requireModification(DSLContext db, HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    if (hubAccess.isTopLevel())
      requireExists("Program Sequence Binding", db.selectCount().from(PROGRAM_SEQUENCE_BINDING)
        .where(PROGRAM_SEQUENCE_BINDING.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Sequence Binding in Program in Account you have hubAccess to", db.selectCount().from(PROGRAM_SEQUENCE_BINDING)
        .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(PROGRAM_SEQUENCE_BINDING.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne(0, int.class));
  }

  /**
   Validate data

   @param record to validate
   @throws DAOException if invalid
   */
  public void validate(ProgramSequenceBinding record) throws DAOException {
    try {
      Value.require(record.getProgramId(), "Program ID");
      Value.require(record.getProgramSequenceId(), "Sequence ID");
      Value.require(record.getOffset(), "Offset");

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

}
