// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.ArrangementDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.arrangement.Arrangement;
import io.outright.xj.core.model.arrangement.ArrangementWrapper;
import io.outright.xj.core.transport.JSON;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Map;

import static io.outright.xj.core.Tables.LINK;
import static io.outright.xj.core.Tables.MORPH;
import static io.outright.xj.core.tables.Arrangement.ARRANGEMENT;
import static io.outright.xj.core.tables.Chain.CHAIN;
import static io.outright.xj.core.tables.Choice.CHOICE;

public class ArrangementDAOImpl extends DAOImpl implements ArrangementDAO {

  @Inject
  public ArrangementDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, ArrangementWrapper data) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, data));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public JSONObject readOne(AccessControl access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public JSONArray readAllIn(AccessControl access, ULong choiceId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllIn(tx.getContext(), access, choiceId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(AccessControl access, ULong id, ArrangementWrapper data) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, data);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(AccessControl access, ULong arrangementId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, arrangementId);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   Create a new record

   @param db     context
   @param access control
   @param data   for new record
   @return newly created record
   @throws BusinessException if a Business Rule is violated
   */
  private JSONObject create(DSLContext db, AccessControl access, ArrangementWrapper data) throws BusinessException {
    Arrangement model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();

    requireTopLevel(access);

    requireRecordExists("Choice", db.select(CHOICE.ID).from(CHOICE)
      .where(CHOICE.ID.eq(model.getChoiceId()))
      .fetchOne());

    return JSON.objectFromRecord(executeCreate(db, ARRANGEMENT, fieldValues));
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private JSONObject readOne(DSLContext db, AccessControl access, ULong id) {
    if (access.isTopLevel()) {
      return JSON.objectFromRecord(db.selectFrom(ARRANGEMENT)
        .where(ARRANGEMENT.ID.eq(id))
        .fetchOne());
    } else {
      return JSON.objectFromRecord(db.select(ARRANGEMENT.fields())
        .from(ARRANGEMENT)
        .join(CHOICE).on(CHOICE.ID.eq(ARRANGEMENT.CHOICE_ID))
        .join(LINK).on(LINK.ID.eq(CHOICE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(ARRANGEMENT.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }
  }

  /**
   Read all records in parent by id

   @param db       context
   @param access   control
   @param choiceId of parent
   @return array of records
   */
  private JSONArray readAllIn(DSLContext db, AccessControl access, ULong choiceId) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.arrayFromResultSet(db.select(ARRANGEMENT.fields())
        .from(ARRANGEMENT)
        .where(ARRANGEMENT.CHOICE_ID.eq(choiceId))
        .fetchResultSet());
    } else {
      return JSON.arrayFromResultSet(db.select(ARRANGEMENT.fields())
        .from(ARRANGEMENT)
        .join(CHOICE).on(CHOICE.ID.eq(ARRANGEMENT.CHOICE_ID))
        .join(LINK).on(LINK.ID.eq(CHOICE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(ARRANGEMENT.CHOICE_ID.eq(choiceId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchResultSet());
    }
  }

  /**
   Update a record

   @param db     context
   @param access control
   @param id     of record
   @param data   to update with
   @throws BusinessException if a Business Rule is violated
   */
  private void update(DSLContext db, AccessControl access, ULong id, ArrangementWrapper data) throws BusinessException, DatabaseException {
    Arrangement model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();
    fieldValues.put(ARRANGEMENT.ID, id);

    requireTopLevel(access);

    requireRecordExists("existing Arrangement with immutable Choice membership",
      db.selectFrom(ARRANGEMENT)
        .where(ARRANGEMENT.ID.eq(id))
        .and(ARRANGEMENT.CHOICE_ID.eq(model.getChoiceId()))
        .fetchOne());

    if (executeUpdate(db, ARRANGEMENT, fieldValues) == 0) {
      throw new BusinessException("No records updated.");
    }
  }

  /**
   Delete a Arrangement

   @param db     context
   @param access control
   @param id     to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private void delete(DSLContext db, AccessControl access, ULong id) throws Exception {
    requireTopLevel(access);

    requireRecordExists("Arrangement", db.selectFrom(ARRANGEMENT)
      .where(ARRANGEMENT.ID.eq(id))
      .fetchOne());

    requireEmptyResultSet(db.select(MORPH.ID)
      .from(MORPH)
      .where(MORPH.ARRANGEMENT_ID.eq(id))
      .fetchResultSet());

    db.deleteFrom(ARRANGEMENT)
      .where(ARRANGEMENT.ID.eq(id))
      .andNotExists(
        db.select(MORPH.ID)
          .from(MORPH)
          .where(MORPH.ARRANGEMENT_ID.eq(id))
      )
      .execute();
  }

}
