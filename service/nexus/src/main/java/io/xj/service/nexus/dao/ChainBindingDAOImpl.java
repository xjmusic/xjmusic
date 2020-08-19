// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.nexus.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.entity.Chain;
import io.xj.service.nexus.entity.ChainBinding;
import io.xj.service.nexus.persistence.NexusEntityStore;
import io.xj.lib.entity.EntityStoreException;

import java.util.Collection;
import java.util.UUID;

/**
 ChainBinding D.A.O. Implementation
 <p>
 Core directive here is to keep business logic CENTRAL ("oneness")
 <p>
 All variants on an update resolve (after recordUpdateFirstStep transformation,
 or additional validation) to one singular central update(fieldValues)
 <p>
 Also note buildNextSegmentOrComplete(...) is one singular central implementation
 of the logic around adding segments to chainBindings and updating chainBinding state to complete.
 <p>
 Nexus DAOs are Singletons unless some other requirement changes that-- 'cuz here be cyclic dependencies...
 */
@Singleton
public class ChainBindingDAOImpl extends DAOImpl<ChainBinding> implements ChainBindingDAO {
  private final ChainDAO chainDAO;

  @Inject
  public ChainBindingDAOImpl(
    EntityFactory entityFactory,
    NexusEntityStore nexusEntityStore,
    ChainDAO chainDAO
  ) {
    super(entityFactory, nexusEntityStore);
    this.chainDAO = chainDAO;
  }

  @Override
  public ChainBinding create(HubClientAccess access, ChainBinding chainBinding) throws DAOFatalException, DAOPrivilegeException {
    try {
      chainBinding.setId(UUID.randomUUID());
      chainBinding.validate();
      requireChainAccountRole(access, chainBinding);
      return store.put(chainBinding);

    } catch (EntityStoreException | ValueException | DAOExistenceException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public ChainBinding readOne(HubClientAccess access, UUID id) throws DAOFatalException, DAOExistenceException, DAOPrivilegeException {
    try {
      ChainBinding chainBinding = store.get(ChainBinding.class, id)
        .orElseThrow(() -> new DAOExistenceException(ChainBinding.class, id));
      requireChainAccountRole(access, chainBinding);
      return chainBinding;

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Collection<ChainBinding> readMany(HubClientAccess access, Collection<UUID> chainIds) throws DAOFatalException, DAOPrivilegeException {
    try {
      for (UUID chainId : chainIds) requireChainAccountRole(access, chainId);
      return store.getAll(ChainBinding.class, Chain.class, chainIds);

    } catch (EntityStoreException | DAOExistenceException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public void update(HubClientAccess access, UUID id, ChainBinding update) throws DAOPrivilegeException {
    throw new DAOPrivilegeException("Not permitted to update Chain Binding. Delete this and create a new one.");
  }

  @Override
  public ChainBinding newInstance() {
    try {
      return entityFactory.getInstance(ChainBinding.class);
    } catch (EntityException ignored) {
      return new ChainBinding();
    }
  }

  @Override
  public void destroy(HubClientAccess access, UUID id) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException {
    try {
      ChainBinding chainBinding = store.get(ChainBinding.class, id)
        .orElseThrow(() -> new DAOExistenceException(ChainBinding.class, id));
      requireChainAccountRole(access, chainBinding);
      store.delete(ChainBinding.class, id);

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  /**
   Fails if user does not have access to the requested chain
   <p>
   Preview-type chains require Artist role
   Production-type chains require Engineer role

   @param access       control
   @param chainBinding to attempt reading, in order to trigger failure in the event that we do not have access
   @throws DAOExistenceException if record does not exist
   */
  private void requireChainAccountRole(HubClientAccess access, ChainBinding chainBinding) throws DAOExistenceException, DAOPrivilegeException, DAOFatalException {
    requireChainAccountRole(access, chainBinding.getChainId());
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
}
