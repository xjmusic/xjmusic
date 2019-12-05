// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.persistence.sql.impl;

import io.xj.core.exception.CoreException;
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

   @throws CoreException on failure.
   */
  public void success() throws CoreException {
    commitAndClose();
  }

  /**
   Success

   @param result to return
   @throws CoreException on failure.
   */
  public <T> T success(T result) throws CoreException {
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

   @throws CoreException on failure.
   */
  private void close() throws CoreException {
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
  private static CoreException failureException(String toDoSomething, Exception e) {
    log.error("Failed {}", toDoSomething);
    return new CoreException(String.format("Failed %s", toDoSomething), e);
  }

  /**
   Commit the transaction.

   @throws CoreException on failure.
   */
  private void commit() throws CoreException {
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

   @throws CoreException if something goes wrong
   */
  private void commitAndClose() throws CoreException {
    try {
      if (isTransaction) {
        commit();
      }
      close();

    } catch (CoreException e) {
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
