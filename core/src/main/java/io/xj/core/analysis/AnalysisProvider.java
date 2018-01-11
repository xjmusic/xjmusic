// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.analysis;

import io.xj.core.access.impl.Access;
import io.xj.core.model.analysis.Analysis;
import io.xj.core.model.analysis.AnalysisType;
import io.xj.core.model.analysis.library_chord.LibraryChordAnalysis;
import io.xj.core.model.analysis.library_meme.LibraryMemeAnalysis;

import org.json.JSONObject;

import java.math.BigInteger;

/**
 [#154234716] Architect wants analysis of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
public interface AnalysisProvider {
  /**
   Perform meme analysis of target library and return resulting JSON Object

   @return analysis results as JSON object
    @param access    control
   @param libraryId of target library to analyze memes of
   */
  LibraryMemeAnalysis analyzeLibraryMemes(Access access, BigInteger libraryId) throws Exception;

  /**
   Perform chord analysis of target library and return resulting JSON Object

   @return analysis results as JSON object
    @param access    control
   @param libraryId of target library to analyze chords of
   */
  LibraryChordAnalysis analyzeLibraryChords(Access access, BigInteger libraryId) throws Exception;
}
