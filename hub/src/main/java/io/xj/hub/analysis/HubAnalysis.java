// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.analysis;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Program;

import java.util.Collection;
import java.util.UUID;

/**
 Template content Analysis https://www.pivotaltracker.com/story/show/161199945
 */
public interface HubAnalysis {

  /**
   Get the access with which this HubAnalysis was instantiated.

   @return access
   */
  HubAccess getAccess();

  /**
   Express all hub content as HTML.

   @return HTML of all hub content
   @throws HubAnalysisException on failure
   */
  String toHTML() throws HubAnalysisException;

  /**
   Get a string representation of analysis
   */
  String toString();

}
