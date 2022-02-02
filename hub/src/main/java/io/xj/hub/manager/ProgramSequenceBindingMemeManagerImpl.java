// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.inject.Inject;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.tables.pojos.ProgramSequenceBindingMeme;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import static io.xj.hub.Tables.*;

public class ProgramSequenceBindingMemeManagerImpl extends HubPersistenceServiceImpl<ProgramSequenceBindingMeme> implements ProgramSequenceBindingMemeManager {

  @Inject
  public ProgramSequenceBindingMemeManagerImpl(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(entityFactory, dbProvider);
  }

  @Override
  public ProgramSequenceBindingMeme create(HubAccess hubAccess, ProgramSequenceBindingMeme rawMeme) throws ManagerException, JsonapiException, ValueException {
    DSLContext db = dbProvider.getDSL();
    var meme = validate(rawMeme);
    requireArtist(hubAccess);
    requireProgramModification(db, hubAccess, meme.getProgramId());

    return modelFrom(ProgramSequenceBindingMeme.class,
      executeCreate(db, PROGRAM_SEQUENCE_BINDING_MEME, meme));
  }

  @Override
  @Nullable
  public ProgramSequenceBindingMeme readOne(HubAccess hubAccess, UUID id) throws ManagerException {
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
  public Collection<ProgramSequenceBindingMeme> readMany(HubAccess hubAccess, Collection<UUID> programIds) throws ManagerException {
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
  public ProgramSequenceBindingMeme update(HubAccess hubAccess, UUID id, ProgramSequenceBindingMeme rawMeme) throws ManagerException, JsonapiException, ValueException {
    DSLContext db = dbProvider.getDSL();
    require("Same id", Objects.equals(id, rawMeme.getId()));
    var meme = validate(rawMeme);
    requireArtist(hubAccess);
    requireProgramModification(db, hubAccess, meme.getProgramId());

    executeUpdate(db, PROGRAM_SEQUENCE_BINDING_MEME, id, meme);
    return meme;
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws ManagerException {
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
   @throws ManagerException if invalid
   */
  public ProgramSequenceBindingMeme validate(ProgramSequenceBindingMeme record) throws ManagerException {
    try {
      Values.require(record.getProgramSequenceBindingId(), "ProgramSequenceBinding ID");
      Values.require(record.getName(), "Name");
      record.setName(Text.toMeme(record.getName()));
      return record;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

}
