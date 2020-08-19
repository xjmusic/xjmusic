// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.nexus.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityStoreException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;
import io.xj.service.nexus.entity.Chain;
import io.xj.service.nexus.entity.ChainConfig;
import io.xj.service.nexus.persistence.NexusEntityStore;

import java.util.Collection;
import java.util.UUID;

/**
 ChainConfig D.A.O. Implementation
 <p>
 Core directive here is to keep business logic CENTRAL ("oneness")
 <p>
 All variants on an update resolve (after recordUpdateFirstStep transformation,
 or additional validation) to one singular central update(fieldValues)
 <p>
 Also note buildNextSegmentOrComplete(...) is one singular central implementation
 of the logic around adding segments to chainConfigs and updating chainConfig state to complete.
 <p>
 Nexus DAOs are Singletons unless some other requirement changes that-- 'cuz here be cyclic dependencies...
 */
@Singleton
public class ChainConfigDAOImpl extends DAOImpl<ChainConfig> implements ChainConfigDAO {
  private final ChainDAO chainDAO;

  @Inject
  public ChainConfigDAOImpl(
    EntityFactory entityFactory,
    NexusEntityStore nexusEntityStore,
    ChainDAO chainDAO
  ) {
    super(entityFactory, nexusEntityStore);
    this.chainDAO = chainDAO;
  }

  @Override
  public ChainConfig create(HubClientAccess access, ChainConfig chainConfig) throws DAOFatalException, DAOPrivilegeException, DAOValidationException, DAOExistenceException {
    try {
      chainConfig.setId(UUID.randomUUID());
      chainConfig.validate();
      requireChainAccountRole(access, chainConfig);
      return store.put(chainConfig);

    } catch (ValueException e) {
      throw new DAOValidationException(e);

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public ChainConfig readOne(HubClientAccess access, UUID id) throws DAOFatalException, DAOExistenceException, DAOPrivilegeException {
    try {
      ChainConfig chainConfig = store.get(ChainConfig.class, id)
        .orElseThrow(() -> new DAOExistenceException(ChainConfig.class, id));
      requireChainAccountRole(access, chainConfig);
      return chainConfig;

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Collection<ChainConfig> readMany(HubClientAccess access, Collection<UUID> chainIds) throws DAOFatalException, DAOPrivilegeException {
    try {
      for (UUID chainId : chainIds) requireChainAccountRole(access, chainId);
      return store.getAll(ChainConfig.class, Chain.class, chainIds);

    } catch (EntityStoreException | DAOExistenceException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public void update(HubClientAccess access, UUID id, ChainConfig update) throws DAOPrivilegeException {
    throw new DAOPrivilegeException("Not permitted to update Chain Config. Delete this and create a new one.");
  }

  @Override
  public void destroy(HubClientAccess access, UUID id) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException {
    try {
      ChainConfig chainConfig = store.get(ChainConfig.class, id)
        .orElseThrow(() -> new DAOExistenceException(ChainConfig.class, id));
      requireChainAccountRole(access, chainConfig);
      store.delete(ChainConfig.class, id);

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  /**
   Fails if user does not have access to the requested chain
   <p>
   Preview-type chains require Artist role
   Production-type chains require Engineer role

   @param access      control
   @param chainConfig to attempt reading, in order to trigger failure in the event that we do not have access
   @throws DAOExistenceException if record does not exist
   */
  private void requireChainAccountRole(HubClientAccess access, ChainConfig chainConfig) throws DAOExistenceException, DAOPrivilegeException, DAOFatalException {
    requireChainAccountRole(access, chainConfig.getChainId());
  }

  /**
   Fails if user does not have access to the requested chain
   <p>
   Preview-type chains require Artist role
   Production-type chains require Engineer role

   @param access  control
   @param chainId to attempt reading, in order to trigger failure in the event that we do not have access
   @throws DAOExistenceException if record does not exist
   */
  private void requireChainAccountRole(HubClientAccess access, UUID chainId) throws DAOExistenceException, DAOPrivilegeException, DAOFatalException {
    Chain chain = chainDAO.readOne(access, chainId);
    chainDAO.requireAccount(access, chain);
  }

  @Override
  public ChainConfig newInstance() {
    try {
      return entityFactory.getInstance(ChainConfig.class);
    } catch (EntityException ignored) {
      return new ChainConfig();
    }
  }
}
