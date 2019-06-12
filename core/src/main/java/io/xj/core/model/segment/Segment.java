//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.segment;

import io.xj.core.exception.CoreException;
import io.xj.core.fabricator.FabricatorType;
import io.xj.core.model.entity.SubEntity;
import io.xj.core.model.entity.SuperEntity;
import io.xj.core.model.program.ProgramType;
import io.xj.core.model.segment.impl.SegmentContent;
import io.xj.core.model.segment.sub.Arrangement;
import io.xj.core.model.segment.sub.Choice;
import io.xj.core.model.segment.sub.SegmentChord;
import io.xj.core.model.segment.sub.SegmentMeme;
import io.xj.core.model.segment.sub.SegmentMessage;
import io.xj.core.model.segment.sub.Pick;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface Segment extends SuperEntity {

  /**
   Add an Arrangement to Segment
   + If there are any exceptions, store them in the SuperEntity errors

   @param arrangement to add
   @return Arrangement with newly added unique-to-segment id
   */
  Arrangement add(Arrangement arrangement);

  /**
   Add a Choice to Segment
   + If there are any exceptions, store them in the SuperEntity errors

   @param choice to add
   @return Choice with newly added unique-to-segment id
   */
  Choice add(Choice choice);

  /**
   Add a Pick to Segment
   + If there are any exceptions, store them in the SuperEntity errors

   @param pick to add
   @return Pick with newly added unique-to-segment id
   */
  Pick add(Pick pick);

  /**
   Add a SegmentChord to Segment
   + If there are any exceptions, store them in the SuperEntity errors

   @param chord to add
   @return SegmentChord with newly added unique-to-segment id
   */
  SegmentChord add(SegmentChord chord);

  /**
   Add a SegmentMeme to Segment
   + If there are any exceptions, store them in the SuperEntity errors

   @param meme to add
   @return SegmentMeme with newly added unique-to-segment id
   */
  SegmentMeme add(SegmentMeme meme);

  /**
   Add a SegmentMessage to Segment
   + If there are any exceptions, store them in the SuperEntity errors

   @param message to add
   @return SegmentMessage with newly added unique-to-segment id
   */
  SegmentMessage add(SegmentMessage message);

  /**
   Get all entities
   + FUTURE: picks are available via API on request, for example to generate detailed visualizations

   @return collection of entities
   */
  Collection<SubEntity> getAllSubEntities();

  /**
   Get Arrangements

   @return Arrangements
   */
  Collection<Arrangement> getArrangements();

  /**
   Get Arrangements for a given Choice

   @param choice to get arrangements for
   @return Arrangements
   */
  Collection<Arrangement> getArrangementsForChoice(Choice choice);

  /**
   Get begin-at time of segment

   @return begin-at time
   */
  Instant getBeginAt();

  /**
   Get Chain ID

   @return chain id
   */
  BigInteger getChainId();

  /**
   Get a Choice by UUID

   @param id of choice
   @return Choice
   @throws CoreException if no such choice exists
   */
  Choice getChoice(UUID id) throws CoreException;

  /**
   Get one Choice of Segment (from Content) of a given type

   @param type of choice to get
   @return choice of given type
   @throws CoreException if no such Choice exists for this Segment
   */
  Choice getChoiceOfType(ProgramType type) throws CoreException;

  /**
   Get Choices

   @return Choices
   */
  Collection<Choice> getChoices();

  /**
   Get all Choices of Segment (from Content) of a given type

   @param type of choice to get
   @return choice of given type
   */
  Collection<Choice> getChoicesOfType(ProgramType type);

  /**
   Get Chords

   @return Chords
   */
  Collection<SegmentChord> getChords();

  /**
   [#162361525] read content of prior Segment
   */
  @Override
  SegmentContent getContent();

  /**
   Get density of segment

   @return density
   */
  Double getDensity();

  /**
   Get end-at time for Segment

   @return end-at time
   */
  Instant getEndAt();

  /**
   Get key for the segment

   @return key
   */
  String getKey();

  /**
   Get Memes

   @return Memes
   */
  Collection<SegmentMeme> getMemes();

  /**
   Get Messages

   @return Messages
   */
  Collection<SegmentMessage> getMessages();

  /**
   Get offset

   @return offset
   */
  Long getOffset();

  /**
   Get Picks

   @return Picks
   */
  Collection<Pick> getPicks();

  /**
   get offset of previous segment

   @return previous segment offset
   */
  Long getPreviousOffset() throws CoreException;

  /**
   Gtr ing

   @return ing
   */
  Map<String, Object> getReport();

  /**
   Get state of Segment

   @return state
   */
  SegmentState getState();

  /**
   Get tempo of segment

   @return tempo
   */
  Double getTempo();

  /**
   Get the total length of the segment

   @return total length
   */
  Integer getTotal();

  /**
   Get Type

   @return Type
   */
  FabricatorType getType();

  /**
   Get waveform key of segment

   @return waveform key
   */
  String getWaveformKey();

  /**
   Whether this Segment is at offset 0

   @return true if offset is 0
   */
  boolean isInitial();

  /**
   Put a key/value into the report

   @param key to put into report
   */
  void putReport(String key, String value);

  /**
   [#158610991] Segment is reverted on failure for fault tolerance of Chain
   [#166293178] Segment revert does not delete any SegmentMessage
   */
  void revert();

  /**
   Set Arrangements
   + If there are any exceptions, store them in the SuperEntity errors

   @param arrangements to set
   */
  void setArrangements(Collection<Arrangement> arrangements);

  /**
   Set begin-at time for Segment

   @param beginAt time to set
   @return this Segment (for chaining setters)
   */
  Segment setBeginAt(String beginAt);

  /**
   Set end-at time for Segment

   @param beginAt time to set
   @return this Segment (for chaining setters)
   */
  Segment setBeginAtInstant(Instant beginAt);

  /**
   Set Chain ID

   @param chainId to set
   @return this Segment (for chaining setters)
   */
  Segment setChainId(BigInteger chainId);

  /**
   Set Choices; copy in contents, to preserve mutability of data persistent internally for this class.
   + If there are any exceptions, store them in the SuperEntity errors

   @param choices to set
   */
  void setChoices(Collection<Choice> choices);

  /**
   Set Chords
   + If there are any exceptions, store them in the SuperEntity errors

   @param chords to set
   */
  void setChords(Collection<SegmentChord> chords);

  /**
   [#162361525] persist Segment content as JSON
   [#166132897] SegmentContent POJO via gson only (no JSONObject)

   @return Segment for chaining setters
   */
  Segment setContent(String json) throws CoreException;

  /**
   Set density of segment

   @param density to set
   @return this Segment (for chaining setters)
   */
  Segment setDensity(Double density);

  /**
   Set end-at time for Segment

   @param endAt time to set
   @return this Segment (for chaining setters)
   */
  Segment setEndAt(String endAt);

  /**
   Set end-at time of Segment

   @param endAt time to set
   @return this Segment (for chaining setters)
   */
  Segment setEndAtInstant(Instant endAt);

  /**
   Set the key for the segment

   @param key to set
   @return this Segment (for chaining setters)
   */
  Segment setKey(String key);

  /**
   Set Memes

   @param memes to set
   */
  void setMemes(Collection<SegmentMeme> memes);

  /**
   Set Messages

   @param messages to set
   */
  void setMessages(Collection<SegmentMessage> messages);

  /**
   Set offset of segment

   @param offset to set
   @return this Segment (for chaining setters)
   */
  Segment setOffset(Long offset);

  /**
   Set Picks

   @param picks to set
   */
  void setPicks(Collection<Pick> picks);

  /**
   Set Report

   @param input to set
   */
  void setReport(Map<String, Object> input);

  /**
   Set state of Segment

   @param value to set
   @return this Segment (for chaining setters)
   */
  Segment setState(String value);

  /**
   Set state of Segment

   @param value to set
   @return this Segment (for chaining setters)
   */
  Segment setStateEnum(SegmentState value);

  /**
   Set tempo of segment

   @param tempo to set
   @return this Segment (for chaining setters)
   */
  Segment setTempo(Double tempo);

  /**
   Set total length of segment

   @param total to set
   @return this Segment (for chaining setters)
   */
  Segment setTotal(Integer total);

  /**
   Set Type

   @param type to set
   */
  void setType(String type);

  /**
   Set TypeEnum

   @param type to set
   */
  void setTypeEnum(FabricatorType type);

  /**
   Set waveform key of segment

   @param waveformKey to set
   @return this Segment (for chaining setters)
   */
  Segment setWaveformKey(String waveformKey);

  /**
   Set created at time

   @param createdAt time
   @return entity
   */
  Segment setCreatedAt(String createdAt);

  /**
   Set created at time

   @param createdAt time
   @return entity
   */
  Segment setCreatedAtInstant(Instant createdAt);

  /**
   Set updated-at time

   @param updatedAt time
   @return entity
   */
  Segment setUpdatedAt(String updatedAt);

  /**
   Set updated-at time

   @param updatedAt time
   @return entity
   */
  Segment setUpdatedAtInstant(Instant updatedAt);
}
