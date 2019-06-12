//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain;

import com.google.inject.assistedinject.Assisted;
import io.xj.core.model.entity.EntityFactory;

import java.math.BigInteger;

/**
 Chain chain = chainFactory.newChain();
 or
 Chain chain = chainFactory.newChain(id);
 */
public interface ChainFactory extends EntityFactory<Chain> {

  /**
   Create a new Chain model

   @param id of new Chain
   @return LibraryWorkMaster
   */
  Chain newChain(
    @Assisted("id") BigInteger id
  );

  /**
   Create a new Chain model

   @return LibraryWorkMaster
   */
  Chain newChain();

}
