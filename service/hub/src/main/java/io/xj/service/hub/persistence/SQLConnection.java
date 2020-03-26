// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.persistence;

import io.xj.service.hub.HubException;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class SQLConnection {
  private static final Logger log = LoggerFactory.getLogger(SQLConnection.class);
  private final DSLContext context;
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

   @throws HubException on failure.
   */
  public void success() throws HubException {
    commitAndClose();
  }

  /**
   Success

   @param result to return
   @throws HubException on failure.
   */
  public <T> T success(T result) throws HubException {
    commitAndClose();
    return result;
  }

  /**
   Failure

   @return CoreException to throw
   */
  public <T> T failure(T e) {
    rollbackAndClose();
    return e;
  }

  /**
   Close the database connection.

   @throws HubException on failure.
   */
  private void close() throws HubException {
    try {
      connection.close();

    } catch (SQLException e) {
      throw failureException("to close connection", e);
    }
  }

  /**
   All Database failure uses this for central logging and exception

   @param toDoSomething that failed "to do something"
   @param e             Exception to encapsulate
   @return CoreException
   */
  private static HubException failureException(String toDoSomething, Exception e) {
    log.error("Failed {}", toDoSomething);
    return new HubException(String.format("Failed %s", toDoSomething), e);
  }

  /**
   Commit the transaction.

   @throws HubException on failure.
   */
  private void commit() throws HubException {
    try {
      connection.commit();

    } catch (SQLException e) {
      try {
        connection.rollback();

      } catch (SQLException e2) {
        log.error("Failed to rollback after failed commit", e2);
      }
      throw failureException("to commit transaction; rolled back OK", e);
    }
  }

  /**
   Commit the transaction, and close the connection.

   @throws HubException if something goes wrong
   */
  private void commitAndClose() throws HubException {
    try {
      if (isTransaction) {
        commit();
      }
      close();

    } catch (HubException e) {
      close();
      throw e;
    }
  }

  /**
   Rollback the transaction, and close the connection.
   */
  private void rollbackAndClose() {
    try {
      if (isTransaction) {
        connection.rollback();
      }
      connection.close();

    } catch (SQLException e) {
      try {
        connection.close();
      } catch (SQLException e2) {
        log.error("Failed to close after failed rollback", e2);
      }
      log.error(isTransaction ? "Failed to rollback and close" : "Failed to close", e);
    }
  }
}
