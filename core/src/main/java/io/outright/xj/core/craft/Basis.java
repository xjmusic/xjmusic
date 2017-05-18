// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.craft;

import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.link.LinkChoice;
import io.outright.xj.core.model.link_meme.LinkMeme;
import io.outright.xj.core.model.meme.Meme;
import io.outright.xj.core.tables.records.IdeaMemeRecord;
import io.outright.xj.core.tables.records.PhaseMemeRecord;
import io.outright.xj.core.tables.records.PhaseRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import java.sql.Timestamp;
import java.util.List;

public interface Basis {

  /**
   Type of Macro-Craft, depending on previous link existence and choices
   */
  enum Type {
    Initial, // the first macro and main ideas in the chain
    Continue, // Main-Idea, if has Phase remaining.
    NextMain, // and Continue Macro-Idea, if has 2 or more phases remaining.
    NextMacro // such that the first phase of the next Macro-Idea will overlap (replacing) the last phase of the current Macro-Idea.
  }

  /**
   Determine type of basis, e.g. initial link in chain, or next macro-idea

   @return macro-craft type
   */
  Type type();

  /**
   The original link submitted for craft

   @return Link
   */
  Link link();

  /**
   is initial link?

   @return whether this is the initial link in a chain
   */
  Boolean isInitialLink();

  /**
   current link id

   @return id of current link
   */
  ULong linkId();

  /**
   Chain id, from link

   @return chain id
   */
  ULong chainId();

  /**
   Link begin-at timestamp

   @return begin at
   */
  Timestamp linkBeginAt();

  /**
   fetch the previous link in the chain
   (caches results)

   @return previousLink
   */
  Link previousLink() throws Exception;

  /**
   fetch the macro-type choice for the previous link in the chain

   @return macro-type link choice
   @throws Exception on failure
   */
  LinkChoice previousMacroChoice() throws Exception;

  /**
   fetch the main-type choice for the previous link in the chain

   @return main-type link choice
   @throws Exception on failure
   */
  LinkChoice previousMainChoice() throws Exception;

  /**
   fetch the rhythm-type choice for the previous link in the chain

   @return rhythm-type link choice
   @throws Exception on failure
   */
  LinkChoice previousRhythmChoice() throws Exception;

  /**
   fetch the macro-type choice for the current link in the chain

   @return macro-type link choice
   @throws Exception on failure
   */
  LinkChoice currentMacroChoice() throws Exception;

  /**
   fetch the main-type choice for the current link in the chain

   @return main-type link choice
   @throws Exception on failure
   */
  LinkChoice currentMainChoice() throws Exception;

  /**
   fetch the rhythm-type choice for the current link in the chain

   @return rhythm-type link choice
   @throws Exception on failure
   */
  LinkChoice currentRhythmChoice() throws Exception;


  /**
   macro-type idea phase in previous link
   (caches results)

   @return phase
   @throws Exception on failure
   */
  PhaseRecord previousMacroPhase() throws Exception;

  /**
   macro-type idea phase in previous link
   (caches results)

   @return phase
   @throws Exception on failure
   */
  PhaseRecord previousMacroNextPhase() throws Exception;

  /**
   Fetch all memes for a given idea
   (caches results)

   @return result of idea memes
   @throws Exception on failure
   */
  Result<IdeaMemeRecord> ideaMemes(ULong ideaId) throws Exception;

  /**
   Fetch all memes for the current link
   (caches results)

   @return link memes
   @throws Exception on failure
   */
  List<Meme> linkMemes() throws Exception;

  /**
   Create a LinkMeme entity by link id and name
   @param linkId  of link meme
   @param memeName of link meme
   @return link meme
   */
  LinkMeme linkMeme(ULong linkId, String memeName);

  /**
   Fetch all memes for a given phase
   (caches results)

   @return result of phase memes
   @throws Exception on failure
   */
  Result<PhaseMemeRecord> phaseMemes(ULong phaseId) throws Exception;

  /**
   Fetch current phase of macro-type idea
   (caches results)

   @return phase record
   @throws Exception on failure
   */
  PhaseRecord phaseByOffset(ULong ideaId, ULong phaseOffset) throws Exception;

  /**
   Fetch a link in a chain, by offset

   @param chainId to fetch link in
   @param offset  of link to fetch
   @return Link
   @throws Exception on failure
   */
  Link linkByOffset(ULong chainId, ULong offset) throws Exception;

  /**
   Fetch current choice of macro-type link
   (caches results)

   @return choice record
   @throws Exception on failure
   */
  LinkChoice linkChoiceByType(ULong linkId, String type) throws Exception;

  /**
   Fetch an idea by idea
   (caches results)

   @param id of idea to fetch
   @return Idea
   @throws Exception on failure
   */
  Idea idea(ULong id) throws Exception;

  /**
   Update the original Link submitted for craft,
   in the internal in-memory object, and persisted in the database

   @param link Link to replace current link, and update database with
   */
  void updateLink(Link link) throws Exception;

  /**
   Put a key-value pair into the report

   @param key   to put
   @param value to put
   */
  void report(String key, String value);

  /**
   Send the final report of craft process, as a link message
   build YAML and create Link Message
   */
  void sendReport();
}
