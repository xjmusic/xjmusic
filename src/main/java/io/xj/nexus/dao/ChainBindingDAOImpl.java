// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.dao;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.api.ChainBinding;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.nexus.NexusException;
import io.xj.nexus.dao.exception.DAOExistenceException;
import io.xj.nexus.dao.exception.DAOFatalException;
import io.xj.nexus.dao.exception.DAOPrivilegeException;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.persistence.NexusEntityStore;

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
  public ChainBinding create(HubClientAccess access, ChainBinding entity) throws DAOFatalException, DAOPrivilegeException {
    try {
      requireChainAccountRole(access, entity);
      entity.setId(UUID.randomUUID());
      validate(entity);
      return store.put(entity);

    } catch (NexusException | ValueException | DAOExistenceException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public ChainBinding readOne(HubClientAccess access, UUID id) throws DAOFatalException, DAOExistenceException, DAOPrivilegeException {
    try {
      var chainBinding = store.getChainBinding(id)
        .orElseThrow(() -> new DAOExistenceException(ChainBinding.class, id.toString()));
      requireChainAccountRole(access, chainBinding);
      return chainBinding;

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Collection<ChainBinding> readMany(HubClientAccess access, Collection<UUID> chainIds) throws DAOFatalException, DAOPrivilegeException {
    try {
      Collection<ChainBinding> chainBindings = Lists.newArrayList();
      for (UUID chainId : chainIds)
        chainBindings.addAll(store.getAllChainBindings(requireChainAccountRole(access, chainId)));
      return chainBindings;

    } catch (NexusException | DAOExistenceException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public ChainBinding update(HubClientAccess access, UUID id, ChainBinding update) throws DAOPrivilegeException {
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
      var chainBinding = store.getChainBinding(id)
        .orElseThrow(() -> new DAOExistenceException(ChainBinding.class, id.toString()));
      requireChainAccountRole(access, chainBinding);
      store.deleteChainBinding(id);

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  /**
   Validate chain binding

   @param record to validate
   @throws ValueException on invalid
   */
  private void validate(ChainBinding record) throws ValueException {
    Value.require(record.getChainId(), "Chain ID");
    Value.require(record.getTargetId(), "Chain-bound target ID");
    Value.require(record.getType(), "Type");
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
   @return chain id (for chaining methods)
   @throws DAOExistenceException if record does not exist
   */
  private UUID requireChainAccountRole(HubClientAccess access, UUID chainId) throws DAOExistenceException, DAOPrivilegeException, DAOFatalException {
    var chain = chainDAO.readOne(access, chainId);
    chainDAO.requireAccount(access, chain);
    return chainId;
  }
}
