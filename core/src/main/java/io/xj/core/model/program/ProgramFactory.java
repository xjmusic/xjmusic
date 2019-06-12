//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.program;

import com.google.inject.assistedinject.Assisted;
import io.xj.core.model.entity.EntityFactory;

import java.math.BigInteger;

/**
 Program program = programFactory.newProgram();
 or
 Program program = programFactory.newProgram(id);
 */
public interface ProgramFactory extends EntityFactory<Program> {

  /**
   Create a new Program model

   @param id of new Program
   @return LibraryWorkMaster
   */
  Program newProgram(
    @Assisted("id") BigInteger id
  );

  /**
   Create a new Program model

   @return LibraryWorkMaster
   */
  Program newProgram();

}
