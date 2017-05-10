// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.db.sql;

import io.outright.xj.core.app.exception.DatabaseException;

import org.jooq.DSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class SQLConnection {

  private static Logger log = LoggerFactory.getLogger(SQLConnection.class);
  protected final DSLContext context;
  private final Boolean isTransaction;
  private final Connection connection;

  SQLConnection(
    Connection connection,
    DSLContext context,
    Boolean isTransaction
  ) {
    this.connection = connection;
    this.context = context;
    this.isTransaction = isTransaction;
  }

  /**
   Get SQL DB Context

   @return SQL DB Context
   */
  public DSLContext getContext() {
    return context;
  }

  /**
   Success- this is the public action,
   and should be overridden by subclasses.

   @throws DatabaseException on failure.
   */
  public void success() throws DatabaseException {
    commitAndClose();
  }

  /**
   Success

   @param result to return
   @throws DatabaseException on failure.
   */
  public <T> T success(T result) throws DatabaseException {
    commitAndClose();
    return result;
  }

  /**
   Failure

   @return DatabaseException to throw
   */
  public <T> T failure(T e) {
    rollbackAndClose();
    return e;
  }

  /**
   Close the database connection.

   @throws DatabaseException on failure.
   */
  private void close() throws DatabaseException {
    try {
      connection.close();
    } catch (Exception eClose) {
      throw failureException("to close connection (" + eClose.toString() + ")");
    }
  }

  /**
   All Database failure uses this for central logging and exception

   @param toDoSomething that failed "to do something"
   @return DatabaseException
   */
  private DatabaseException failureException(String toDoSomething) {
    logFailed(toDoSomething);
    return new DatabaseException("Failed " + toDoSomething);
  }

  /**
   All Database failure uses this for central logging

   @param toDoSomething that failed "to do something"
   */
  private void logFailed(String toDoSomething) {
    log.error("Failed " + toDoSomething);
  }

  /**
   Commit the database transaction.

   @throws DatabaseException on failure.
   */
  private void commit() throws DatabaseException {
    try {
      connection.commit();
    } catch (Exception eCommit) {
      try {
        connection.rollback();
        throw failureException(
          "to commit transaction (" + eCommit.toString() + "); " +
            "rolled back OK");
      } catch (Exception eRollback) {
        throw failureException(
          "to commit database transaction (" + eCommit.toString() + ", " +
            "to rollback (" + eRollback.toString() + ") ");
      }
    }
  }

  /**
   Commit the SQL Database transaction and close the connection.

   @throws DatabaseException if something goes wrong
   */
  private void commitAndClose() throws DatabaseException {
    try {
      if (isTransaction) {
        commit();
      }
      close();
    } catch (Exception e) {
      close();
      throw e;
    }
  }

  /**
   Commit the SQL Database transaction and close the connection. Eats exceptions, to simplify implementation logic since this will be the final action taken.
   */
  private void rollbackAndClose() {
    try {
      if (isTransaction) {
        connection.rollback();
      }
      connection.close();
    } catch (SQLException eRollbackClose) {
      try {
        connection.close();
        logFailed(
          "to rollback and close (" + eRollbackClose.toString() + "); " +
            "closed OK");
      } catch (SQLException eClose) {
        logFailed(
          "to rollback and close (" + eRollbackClose.toString() + "), " +
            "to close (" + eClose.toString() + ")");
      }
    }
  }
}
