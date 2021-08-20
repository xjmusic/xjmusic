// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.inject.Inject;
import io.xj.api.ProgramSequenceBindingMeme;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import static io.xj.hub.Tables.LIBRARY;
import static io.xj.hub.Tables.PROGRAM;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_BINDING_MEME;

public class ProgramSequenceBindingMemeDAOImpl extends DAOImpl<ProgramSequenceBindingMeme> implements ProgramSequenceBindingMemeDAO {

  @Inject
  public ProgramSequenceBindingMemeDAOImpl(
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
  }

  @Override
  public ProgramSequenceBindingMeme create(HubAccess hubAccess, ProgramSequenceBindingMeme rawMeme) throws DAOException, JsonapiException, ValueException {
    DSLContext db = dbProvider.getDSL();
    var meme = validate(rawMeme);
    requireArtist(hubAccess);
    requireProgramModification(db, hubAccess, meme.getProgramId());

    return modelFrom(ProgramSequenceBindingMeme.class,
      executeCreate(db, PROGRAM_SEQUENCE_BINDING_MEME, meme));
  }

  @Override
  @Nullable
  public ProgramSequenceBindingMeme readOne(HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    if (hubAccess.isTopLevel())
      return modelFrom(ProgramSequenceBindingMeme.class,
        dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_BINDING_MEME)
          .where(PROGRAM_SEQUENCE_BINDING_MEME.ID.eq(id))
          .fetchOne());
    else
      return modelFrom(ProgramSequenceBindingMeme.class,
        dbProvider.getDSL().select(PROGRAM_SEQUENCE_BINDING_MEME.fields()).from(PROGRAM_SEQUENCE_BINDING_MEME)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_BINDING_MEME.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramSequenceBindingMeme> readMany(HubAccess hubAccess, Collection<UUID> programIds) throws DAOException {
    requireArtist(hubAccess);
    if (hubAccess.isTopLevel())
      return modelsFrom(ProgramSequenceBindingMeme.class,
        dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_BINDING_MEME)
          .where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID.in(programIds))
          .fetch());
    else
      return modelsFrom(ProgramSequenceBindingMeme.class,
        dbProvider.getDSL().select(PROGRAM_SEQUENCE_BINDING_MEME.fields()).from(PROGRAM_SEQUENCE_BINDING_MEME)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID.in(programIds))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetch());
  }

  @Override
  public ProgramSequenceBindingMeme update(HubAccess hubAccess, UUID id, ProgramSequenceBindingMeme rawMeme) throws DAOException, JsonapiException, ValueException {
    DSLContext db = dbProvider.getDSL();
    require("Same id", Objects.equals(id, rawMeme.getId()));
    var meme = validate(rawMeme);
    requireArtist(hubAccess);
    requireProgramModification(db, hubAccess, meme.getProgramId());

    executeUpdate(db, PROGRAM_SEQUENCE_BINDING_MEME, id, meme);
    return meme;
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {
    DSLContext db = dbProvider.getDSL();
    requireArtist(hubAccess);

    if (!hubAccess.isTopLevel())
      requireExists("Meme belongs to Program in Account you have hubAccess to", db.selectCount()
        .from(PROGRAM_SEQUENCE_BINDING_MEME)
        .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(PROGRAM_SEQUENCE_BINDING_MEME.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(PROGRAM_SEQUENCE_BINDING_MEME)
      .where(PROGRAM_SEQUENCE_BINDING_MEME.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramSequenceBindingMeme newInstance() {
    return new ProgramSequenceBindingMeme();
  }

  /**
   Validate data

   @param record to validate
   @throws DAOException if invalid
   */
  public ProgramSequenceBindingMeme validate(ProgramSequenceBindingMeme record) throws DAOException {
    try {
      Value.require(record.getProgramSequenceBindingId(), "ProgramSequenceBinding ID");
      Value.require(record.getName(), "Name");
      record.setName(Text.toMeme(record.getName()));
      return record;

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

}
