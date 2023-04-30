// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.tables.pojos.InstrumentMeme;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.Tables.INSTRUMENT_MEME;

@Service
public class InstrumentMemeManagerImpl extends HubPersistenceServiceImpl implements InstrumentMemeManager {

  public InstrumentMemeManagerImpl(
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider
  ) {
    super(entityFactory, sqlStoreProvider);
  }

  @Override
  public InstrumentMeme create(HubAccess access, InstrumentMeme rawMeme) throws ManagerException, JsonapiException, ValueException {
    var meme = validate(rawMeme);
    requireArtist(access);
    return modelFrom(InstrumentMeme.class,
      executeCreate(sqlStoreProvider.getDSL(), INSTRUMENT_MEME, meme));
  }

  @Override
  @Nullable
  public InstrumentMeme readOne(HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);
    try (var selectInstrumentMeme = sqlStoreProvider.getDSL().selectFrom(INSTRUMENT_MEME)) {
      return modelFrom(InstrumentMeme.class,
        selectInstrumentMeme
          .where(INSTRUMENT_MEME.ID.eq(id))
          .fetchOne());
    } catch (Exception e) {
      throw new ManagerException(e);
    }
  }

  @Override
  @Nullable
  public Collection<InstrumentMeme> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    requireArtist(access);
    try (var selectInstrumentMeme = sqlStoreProvider.getDSL().selectFrom(INSTRUMENT_MEME)){
    return modelsFrom(InstrumentMeme.class,
      selectInstrumentMeme
        .where(INSTRUMENT_MEME.INSTRUMENT_ID.in(parentIds))
        .fetch());
    } catch (Exception e) {
      throw new ManagerException(e);
    }
  }

  @Override
  public InstrumentMeme update(HubAccess access, UUID id, InstrumentMeme rawMeme) throws ManagerException, JsonapiException, ValueException {
    var meme = validate(rawMeme);
    requireArtist(access);
    executeUpdate(sqlStoreProvider.getDSL(), INSTRUMENT_MEME, id, meme);
    return meme;
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);
    try (var deleteInstrumentMeme =sqlStoreProvider.getDSL().deleteFrom(INSTRUMENT_MEME)) {
    deleteInstrumentMeme
      .where(INSTRUMENT_MEME.ID.eq(id))
      .execute();
    } catch (Exception e) {
      throw new ManagerException(e);
    }
  }

  @Override
  public InstrumentMeme newInstance() {
    return new InstrumentMeme();
  }

  /**
   Validate data

   @param record to validate
   @throws ManagerException if invalid
   */
  public InstrumentMeme validate(InstrumentMeme record) throws ManagerException {
    try {
      Values.require(record.getInstrumentId(), "Instrument ID");
      Values.require(record.getName(), "Name");
      record.setName(Text.toMeme(record.getName()));
      return record;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

}
