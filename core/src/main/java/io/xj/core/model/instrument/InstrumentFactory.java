//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.instrument;

import com.google.inject.assistedinject.Assisted;
import io.xj.core.model.entity.EntityFactory;

import java.math.BigInteger;

/**
 Instrument instrument = instrumentFactory.newInstrument();
 or
 Instrument instrument = instrumentFactory.newInstrument(id);
 */
public interface InstrumentFactory extends EntityFactory<Instrument> {

  /**
   Create a new Instrument model

   @param id of new Instrument
   @return LibraryWorkMaster
   */
  Instrument newInstrument(
    @Assisted("id") BigInteger id
  );

  /**
   Create a new Instrument model

   @return LibraryWorkMaster
   */
  Instrument newInstrument();

}
