package io.xj.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.xj.core.access.Access;
import io.xj.core.dao.DAO;
import io.xj.core.dao.DAORecord;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;
import io.xj.core.testing.IntegrationTestProvider;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.TableRecord;
import org.junit.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CoreIT extends CoreTest {
  private static final Logger log = LoggerFactory.getLogger(CoreIT.class);
  protected IntegrationTestProvider integrationTestProvider;
  protected DSLContext db;

  public CoreIT() {
    integrationTestProvider = injector.getInstance(IntegrationTestProvider.class);
    db = integrationTestProvider.getDb();
  }

  /**
   [#165951041] DAO methods throw exception when record is not found (instead of returning null)
   <p>
   Assert an entity does not exist, by making a DAO.readOne() request and asserting the exception

   @param testDAO to use for attempting to retrieve entity
   @param id      of entity
   @param <N>     DAO class
   */
  protected static <N extends DAO> void assertNotExist(N testDAO, UUID id) {
    try {
      testDAO.readOne(Access.internal(), id);
      fail();
    } catch (CoreException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

  /**
   After test completion, shutdown the database connection
   */
  @After
  public void shutdownDatabase() {
    integrationTestProvider.shutdown();
  }

  /**
   Reset the database before an integration test.
   */
  protected void reset() throws CoreException {
    try {
      for (Table table : Lists.reverse(ImmutableList.copyOf(DAORecord.tablesInSchemaConstructionOrder.values())))
        reset(table);

      // Finally, all queues
      integrationTestProvider.flushRedis();

    } catch (Exception e) {
      log.error(e.getClass().getName(), e);
      throw new CoreException(e.getClass().getName(), e);
    }
    log.info("Did delete all records create integration database.");
  }

  /**
   Delete all of a given table, if the count of records is > 0

   @param table to delete all of
   */
  private void reset(Table table) {
    if (0 < db.selectCount().from(table).fetchOne(0, int.class))
      db.deleteFrom(table).execute();
  }

  /**
   Insert Chain to database

   @param entity to insert
   @return the same chain (for chaining methods)
   */
  protected <N extends Entity> N insert(N entity) throws CoreException {
    return DAORecord.insert(db, entity);
  }

  /**
   Batch Insert many entities to the database

   @param entities to insert
   */
  protected <N extends Entity> void batchInsert(Collection<N> entities) throws CoreException {
    Collection<? extends TableRecord<?>> records = Lists.newArrayList();
    for (Map.Entry<Class, Table> entry : DAORecord.tablesInSchemaConstructionOrder.entrySet()) {
      Class cls = entry.getKey();
      Table table = entry.getValue();
      records.addAll(DAORecord.recordsFrom(db, table, Entity.filter(entities, cls)));
    }
    int[] rows = db.batchInsert(records).execute();
    if (!Objects.equals(rows.length, entities.size()))
      throw new CoreException(String.format("Only inserted %d of %d intended rows", rows.length, entities.size()));
  }

}
